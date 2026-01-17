import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/rooms': 'http://localhost:8085',
      '/types': 'http://localhost:8085',
      '/checkpoints': 'http://localhost:8085',
      '/schedules': 'http://localhost:8085',
      '/test': 'http://localhost:8085',
      '/users': 'http://localhost:8085',
      '/approvals': 'http://localhost:8085',
      '/students': 'http://localhost:8085',
      '/attendances': 'http://localhost:8085',
    },
  },
})
