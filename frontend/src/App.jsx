import { Routes, Route, Navigate } from 'react-router-dom'
import JoinContestPage from './pages/JoinContestPage'
import ContestPage from './pages/ContestPage'
import AdminPage from './pages/AdminPage'

export default function App() {
  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <Routes>
        <Route path="/" element={<JoinContestPage />} />
        <Route path="/contest/:contestId" element={<ContestPage />} />
  <Route path="/admin" element={<AdminPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}
