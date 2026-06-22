import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// ---------------------------------------------------------------------------
// Benchmark hieu nang cac endpoint catalog co cache (Redis).
//
// Chay (Docker, khong can cai k6):
//   # cache ON  -> app chay binh thuong
//   docker run --rm -i -v "${PWD}/k6:/scripts" -e BASE_URL=http://host.docker.internal:8081 \
//       grafana/k6 run /scripts/cache-benchmark.js
//
//   # cache OFF -> chay app voi: --spring.cache.type=none o cong khac, roi doi BASE_URL
//
// So sanh RPS (http_reqs), p95/p99 (http_req_duration), loi (http_req_failed)
// giua hai lan -> chenh lech chinh la loi ich cache duoi tai.
// ---------------------------------------------------------------------------

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';

// Cac endpoint public GET co gan @Cacheable (SecurityConfig.PUBLIC_ENDPOINTS_GET)
const ENDPOINTS = ['/movies', '/cinemas', '/schedule', '/foodanddrink'];

const non200 = new Counter('non_200_responses');

export const options = {
  scenarios: {
    ramping_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 50 },  // tang dan toi 50 VU
        { duration: '45s', target: 50 },  // giu tai
        { duration: '10s', target: 0 },   // ha tai
      ],
      gracefulRampDown: '5s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],          // < 1% loi
    http_req_duration: ['p(95)<150', 'p(99)<400'],
  },
};

export default function () {
  // moi VU xoay vong qua cac endpoint de phan bo deu tai
  const path = ENDPOINTS[__ITER % ENDPOINTS.length];
  const res = http.get(`${BASE_URL}${path}`, { tags: { endpoint: path } });

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
  });
  if (!ok) {
    non200.add(1);
  }
}

export function handleSummary(data) {
  return {
    stdout: textSummary(data),
    '/scripts/summary.json': JSON.stringify(data, null, 2),
  };
}

// Tom tat gon gang ra stdout (tranh phu thuoc thu vien ngoai)
function textSummary(data) {
  const m = data.metrics;
  const get = (name, field, dflt = 0) =>
    m[name] && m[name].values && m[name].values[field] !== undefined
      ? m[name].values[field]
      : dflt;

  const reqs = get('http_reqs', 'count');
  const rps = get('http_reqs', 'rate');
  const p95 = get('http_req_duration', 'p(95)');
  const p99 = get('http_req_duration', 'p(99)');
  const avg = get('http_req_duration', 'avg');
  const failRate = get('http_req_failed', 'rate');

  return [
    '',
    '=============== CACHE BENCHMARK SUMMARY ===============',
    `  BASE_URL          : ${BASE_URL}`,
    `  total requests    : ${reqs}`,
    `  throughput (RPS)  : ${rps.toFixed(1)}`,
    `  latency avg       : ${avg.toFixed(1)} ms`,
    `  latency p95       : ${p95.toFixed(1)} ms`,
    `  latency p99       : ${p99.toFixed(1)} ms`,
    `  error rate        : ${(failRate * 100).toFixed(2)} %`,
    '======================================================',
    '',
  ].join('\n');
}
