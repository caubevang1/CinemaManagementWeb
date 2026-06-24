import { http } from '../utils/baseUrl'

export const getSystemHealth = () => http.get('/actuator/health')
