import { useEffect, useMemo, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { api } from '../api'
import Leaderboard from '../components/Leaderboard'

export default function ContestPage() {
  const { contestId } = useParams()
  const [searchParams] = useSearchParams()
  const username = searchParams.get('username')

  const [contest, setContest] = useState(null)
  const [problems, setProblems] = useState([])
  const [selectedProblemId, setSelectedProblemId] = useState(null)
  const [code, setCode] = useState('public class Main {\n  public static void main(String[] args) throws Exception {\n    java.util.Scanner sc = new java.util.Scanner(System.in);\n    // TODO: write solution here\n  }\n}')
  const [lastSubmission, setLastSubmission] = useState(null)
  const [leaderboard, setLeaderboard] = useState([])

  // Fetch contest
  useEffect(() => {
    // reset state when contest changes
    setContest(null)
    setProblems([])
    setSelectedProblemId(null)
    setLastSubmission(null)
    api.get(`/api/contests/${contestId}`)
      .then(res => {
        setContest(res.data)
        setProblems(res.data.problems || [])
        if (res.data.problems?.length) setSelectedProblemId(res.data.problems[0].id)
      })
      .catch(() => { setContest({ error: true }); setProblems([]); setSelectedProblemId(null) })
  }, [contestId])

  // Poll leaderboard every 15s
  useEffect(() => {
    let timer
    const fetchLb = () => api.get(`/api/contests/${contestId}/leaderboard`).then(res => setLeaderboard(res.data)).catch(()=>{})
    fetchLb()
    timer = setInterval(fetchLb, 15000)
    return () => clearInterval(timer)
  }, [contestId])

  // Poll submission status every 3s
  useEffect(() => {
    if (!lastSubmission?.id) return
    const timer = setInterval(() => {
      api.get(`/api/submissions/${lastSubmission.id}`).then(res => setLastSubmission(res.data)).catch(()=>{})
    }, 3000)
    return () => clearInterval(timer)
  }, [lastSubmission?.id])

  const submit = async () => {
    if (!selectedProblemId || !username) return alert('Pick a problem and include username in URL')
    const payload = { contestId: Number(contestId), problemId: Number(selectedProblemId), username, sourceCode: code }
    const { data } = await api.post('/api/submissions', payload)
    setLastSubmission(data)
  }

  const selectedProblem = useMemo(() => problems.find(p => p.id === Number(selectedProblemId)), [problems, selectedProblemId])

  return (
    <div className="p-4 max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-4">
      <div className="md:col-span-2 space-y-4">
        <div className="bg-white rounded shadow p-4">
          <h2 className="text-xl font-semibold">{contest?.error ? 'Contest not found' : (contest?.title || 'Contest')}</h2>
          {contest?.error && (
            <p className="text-sm text-red-600 mt-1">This contest ID does not exist. Try ID 1 (seeded) or go back to the join page.</p>
          )}
        </div>
        <div className="bg-white rounded shadow p-4">
          <div className="flex items-center gap-3 mb-2">
            <label className="text-sm">Problem:</label>
            <select className="border rounded px-2 py-1" value={selectedProblemId ?? ''} onChange={e=>setSelectedProblemId(e.target.value)} disabled={!problems.length}>
              {problems.map(p => <option key={p.id} value={p.id}>{p.title}</option>)}
            </select>
            <div className="text-sm text-gray-600">User: <span className="font-mono">{username}</span></div>
          </div>
          <div className="prose max-w-none">
            <h3 className="font-semibold mb-1">Statement</h3>
            <p className="whitespace-pre-wrap">{selectedProblem?.statement}</p>
          </div>
        </div>
        <div className="bg-white rounded shadow p-4">
          <h3 className="font-semibold mb-2">Code</h3>
          <textarea value={code} onChange={e=>setCode(e.target.value)} className="w-full h-64 font-mono text-sm border rounded p-2"/>
          <div className="mt-3 flex items-center gap-3">
            <button onClick={submit} className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">Submit</button>
            {lastSubmission && (
              <span className="text-sm">Last submission: <span className="font-semibold">{lastSubmission.status}</span></span>
            )}
          </div>
          {lastSubmission?.message && (
            <pre className="mt-2 text-xs bg-gray-50 p-2 rounded border whitespace-pre-wrap">{lastSubmission.message}</pre>
          )}
        </div>
      </div>
      <div className="space-y-4">
        <Leaderboard entries={leaderboard} />
      </div>
    </div>
  )
}
