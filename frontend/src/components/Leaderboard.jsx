export default function Leaderboard({ entries }) {
  return (
    <div className="bg-white rounded shadow p-3">
      <h3 className="font-semibold mb-2">Leaderboard</h3>
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left border-b">
            <th className="py-1">User</th>
            <th className="py-1">Solved</th>
            <th className="py-1">Last Accepted</th>
          </tr>
        </thead>
        <tbody>
          {entries?.map((e, idx) => (
            <tr key={idx} className="border-b">
              <td className="py-1">{e.username}</td>
              <td className="py-1">{e.solved}</td>
              <td className="py-1">{e.lastAcceptedAt ? new Date(e.lastAcceptedAt).toLocaleTimeString() : '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
