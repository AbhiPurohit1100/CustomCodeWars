import { useEffect, useState } from 'react'
import { api } from '../api'
import { Link } from 'react-router-dom'

export default function AdminPage() {
  const [contests, setContests] = useState([])
  const [newContestTitle, setNewContestTitle] = useState('')
  const [selectedContestId, setSelectedContestId] = useState('')

  const [problemTitle, setProblemTitle] = useState('')
  const [problemStatement, setProblemStatement] = useState('')
  const [createdProblemId, setCreatedProblemId] = useState(null)

  const [tcInput, setTcInput] = useState('')
  const [tcExpected, setTcExpected] = useState('')
  const [tcOrder, setTcOrder] = useState('')
  const [testcases, setTestcases] = useState([])

  const refreshContests = () => api.get('/api/contests').then(res => setContests(res.data)).catch(()=>{})

  useEffect(() => { refreshContests() }, [])

  const createContest = async () => {
    if (!newContestTitle.trim()) return
    await api.post('/api/admin/contests', { title: newContestTitle.trim() })
    setNewContestTitle('')
    refreshContests()
  }

  const createProblem = async () => {
    if (!selectedContestId || !problemTitle.trim() || !problemStatement.trim()) return
    const { data } = await api.post(`/api/admin/contests/${selectedContestId}/problems`, {
      title: problemTitle.trim(),
      statement: problemStatement.trim()
    })
    setCreatedProblemId(data.id)
    setProblemTitle('')
    setProblemStatement('')
    setTestcases([])
  }

  const addTestcase = async () => {
    if (!createdProblemId || !tcInput.length || !tcExpected.length) return
    const payload = { inputText: tcInput, expectedOutput: tcExpected }
    if (tcOrder) payload.orderIndex = Number(tcOrder)
    const { data } = await api.post(`/api/admin/problems/${createdProblemId}/tests`, payload)
    setTestcases(prev => [...prev, { id: data.id, inputText: tcInput, expectedOutput: tcExpected, orderIndex: tcOrder || testcases.length+1 }])
    setTcInput(''); setTcExpected(''); setTcOrder('')
  }

  return (
    <div className="max-w-5xl mx-auto p-4 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">Admin: Manage Contests & Problems</h1>
        <Link className="text-blue-600 underline" to="/">Go to Join</Link>
      </div>

      <section className="bg-white rounded shadow p-4 space-y-3">
        <h2 className="font-semibold text-lg">Create Contest</h2>
        <div className="flex gap-2">
          <input className="border rounded px-3 py-2 flex-1" placeholder="Contest title" value={newContestTitle} onChange={e=>setNewContestTitle(e.target.value)} />
          <button onClick={createContest} className="bg-blue-600 text-white px-4 py-2 rounded">Create</button>
        </div>
        <div className="text-sm text-gray-700">Existing: {contests.map(c => (<span key={c.id} className="mr-3">{c.title} (ID {c.id})</span>))}</div>
      </section>

      <section className="bg-white rounded shadow p-4 space-y-3">
        <h2 className="font-semibold text-lg">Create Problem</h2>
        <div className="flex items-center gap-2">
          <label className="text-sm">Contest:</label>
          <select className="border rounded px-2 py-1" value={selectedContestId} onChange={e=>setSelectedContestId(e.target.value)}>
            <option value="">Select...</option>
            {contests.map(c => <option key={c.id} value={c.id}>{c.title} (ID {c.id})</option>)}
          </select>
        </div>
        <input className="border rounded px-3 py-2 w-full" placeholder="Problem title" value={problemTitle} onChange={e=>setProblemTitle(e.target.value)} />
        <textarea className="border rounded px-3 py-2 w-full h-32" placeholder="Problem statement" value={problemStatement} onChange={e=>setProblemStatement(e.target.value)} />
        <button onClick={createProblem} className="bg-green-600 text-white px-4 py-2 rounded">Create Problem</button>
        {createdProblemId && (
          <div className="text-sm text-gray-700">Created problem ID: <span className="font-mono">{createdProblemId}</span></div>
        )}
      </section>

      <section className="bg-white rounded shadow p-4 space-y-3">
        <h2 className="font-semibold text-lg">Add Test Case{createdProblemId ? ` for Problem ${createdProblemId}` : ''}</h2>
        {!createdProblemId && <div className="text-sm text-red-600">Create a problem first.</div>}
        <textarea className="border rounded px-3 py-2 w-full h-20 font-mono" placeholder="Input (stdin)" value={tcInput} onChange={e=>setTcInput(e.target.value)} />
        <textarea className="border rounded px-3 py-2 w-full h-20 font-mono" placeholder="Expected output" value={tcExpected} onChange={e=>setTcExpected(e.target.value)} />
        <div className="flex items-center gap-2">
          <label className="text-sm">Order:</label>
          <input className="border rounded px-2 py-1 w-24" type="number" min="1" value={tcOrder} onChange={e=>setTcOrder(e.target.value)} />
          <button onClick={addTestcase} disabled={!createdProblemId} className="bg-purple-600 text-white px-4 py-2 rounded disabled:opacity-50">Add Test</button>
        </div>
        {testcases.length > 0 && (
          <div>
            <h3 className="font-semibold">Current Test Cases</h3>
            <ul className="list-disc ml-6 text-sm">
              {testcases.map(t => (<li key={t.id}><span className="text-gray-600">[{t.orderIndex}]</span> input= <code>{JSON.stringify(t.inputText)}</code> → expect= <code>{JSON.stringify(t.expectedOutput)}</code></li>))}
            </ul>
          </div>
        )}
      </section>
    </div>
  )
}
