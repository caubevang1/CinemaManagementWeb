import React, { useEffect, useState } from 'react'
import { Badge, Card, Col, Row, Spin, Tag, Typography } from 'antd'
import { getSystemHealth } from '../../services/AdminService'

const { Title, Text } = Typography

const STATUS_TAG = {
  UP:        { color: 'success',    label: 'UP' },
  DOWN:      { color: 'error',      label: 'DOWN' },
  CLOSED:    { color: 'success',    label: 'CLOSED' },
  OPEN:      { color: 'warning',    label: 'OPEN' },
  HALF_OPEN: { color: 'processing', label: 'HALF-OPEN' },
}

function StatusTag({ status }) {
  const cfg = STATUS_TAG[status] ?? { color: 'default', label: status ?? 'N/A' }
  return <Tag color={cfg.color}>{cfg.label}</Tag>
}

export default function Dashboard() {
  const [health, setHealth] = useState(null)
  const [loading, setLoading] = useState(true)
  const [lastRefresh, setLastRefresh] = useState(null)

  const fetchHealth = async () => {
    try {
      const res = await getSystemHealth()
      setHealth(res.data)
      setLastRefresh(new Date())
    } catch (err) {
      // Actuator trả HTTP 503 khi tổng status = DOWN, nhưng body vẫn có đầy đủ
      // breakdown từng component → dùng nó thay vì để trắng toàn bộ.
      const body = err?.response?.data
      if (body?.components) {
        setHealth(body)
        setLastRefresh(new Date())
      } else {
        // Chỉ null khi thực sự không có response (backend tắt hẳn)
        setHealth(null)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchHealth()
    const id = setInterval(fetchHealth, 30_000)
    return () => clearInterval(id)
  }, [])

  const comp = health?.components ?? {}
  const cbDetails = comp.circuitBreakers?.components ?? comp.circuitBreakers?.details ?? {}

  const infraItems = [
    { label: 'MySQL',    key: 'db',     status: comp.db?.status },
    { label: 'Redis',    key: 'redis',  status: comp.redis?.status },
    { label: 'RabbitMQ', key: 'rabbit', status: comp.rabbit?.status },
  ]

  const cbItems = [
    { label: 'Cloudinary', key: 'cloudinary' },
    { label: 'Email',      key: 'email' },
    { label: 'TMDB',       key: 'tmdb' },
  ].map(item => ({
    ...item,
    state: cbDetails[item.key]?.details?.state ?? cbDetails[item.key]?.status,
  }))

  return (
    <div className="p-6">
      <Spin spinning={loading}>
        {/* Overall status */}
        <Card className="mb-4">
          <div className="flex items-center gap-3">
            <Badge
              status={health?.status === 'UP' ? 'success' : 'error'}
              text={
                <span className="font-semibold text-base">
                  System: <StatusTag status={health?.status} />
                </span>
              }
            />
            {lastRefresh && (
              <Text type="secondary" className="ml-auto text-xs">
                Cập nhật lúc {lastRefresh.toLocaleTimeString()}
              </Text>
            )}
          </div>
        </Card>

        {/* Infrastructure */}
        <Title level={5} className="mb-3">Hạ tầng</Title>
        <Row gutter={[16, 16]} className="mb-6">
          {infraItems.map(item => (
            <Col xs={24} sm={8} key={item.key}>
              <Card size="small" title={item.label}>
                <StatusTag status={item.status} />
              </Card>
            </Col>
          ))}
        </Row>

        {/* Circuit Breakers */}
        <Title level={5} className="mb-3">Circuit Breakers</Title>
        <Row gutter={[16, 16]}>
          {cbItems.map(item => (
            <Col xs={24} sm={8} key={item.key}>
              <Card size="small" title={item.label}>
                <StatusTag status={item.state} />
              </Card>
            </Col>
          ))}
        </Row>
      </Spin>
    </div>
  )
}
