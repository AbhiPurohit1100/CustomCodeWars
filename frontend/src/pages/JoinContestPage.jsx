import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api'

export default function JoinContestPage() {
  const [contestId, setContestId] = useState('1')
  const [username, setUsername] = useState('')
  const navigate = useNavigate()
  const [contests, setContests] = useState([])

  useEffect(() => {
    api.get('/api/contests').then(res => setContests(res.data)).catch(()=>{})
  }, [])

  const join = (e) => {
    e.preventDefault()
    if (!contestId || !username) return
    navigate(`/contest/${encodeURIComponent(contestId)}?username=${encodeURIComponent(username)}`)
  }

  return (
    <div className="flex items-center justify-center h-screen">
      <form onSubmit={join} className="bg-white shadow p-6 rounded w-full max-w-md space-y-4">
        <h1 className="text-2xl font-semibold">Join Contest</h1>
        <div className="text-right -mt-4">
          <a className="text-blue-600 underline text-sm" href="/admin">Admin</a>
        </div>
        <div>
          <label className="block text-sm mb-1">Contest ID</label>
          <input value={contestId} onChange={e=>setContestId(e.target.value)} className="w-full border rounded px-3 py-2" placeholder="1" />
        </div>
        {contests.length > 0 && (
          <div className="text-sm text-gray-700">
            Available: {contests.map((c, idx) => (
              <button key={c.id} type="button" className="underline text-blue-600 mr-2" onClick={()=>setContestId(String(c.id))}>
                {c.title} (ID {c.id})
              </button>
            ))}
          </div>
        )}
        <div>
          <label className="block text-sm mb-1">Username</label>
          <input value={username} onChange={e=>setUsername(e.target.value)} className="w-full border rounded px-3 py-2" placeholder="alice" />
        </div>
        <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">Join Contest</button>
      </form>
    </div>
  )
}
