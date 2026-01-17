import { useState, useEffect } from 'react';
import type { RoomApproval, Room } from '../types';
import { roomApprovalApi, roomApi } from '../api/client';

type Filter = 'all' | 'pending' | 'approved';

export function RoomApprovalPanel() {
  const [approvals, setApprovals] = useState<RoomApproval[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState<Filter>('all');
  const [selectedRoomId, setSelectedRoomId] = useState<number>(0);

  const fetchApprovals = async () => {
    setLoading(true);
    try {
      const approved = filter === 'all' ? undefined : filter === 'approved';
      const data = await roomApprovalApi.getAll(approved);
      setApprovals(data || []);
    } catch (e) {
      console.error('Failed to fetch approvals:', e);
      setApprovals([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchRooms = async () => {
    try {
      const data = await roomApi.getAll();
      setRooms(data);
    } catch (e) {
      console.error('Failed to fetch rooms:', e);
    }
  };

  useEffect(() => {
    fetchApprovals();
  }, [filter]);

  useEffect(() => {
    fetchRooms();
  }, []);

  const handleCreate = async () => {
    if (!selectedRoomId) {
      alert('실을 선택해주세요');
      return;
    }
    try {
      await roomApprovalApi.create(selectedRoomId);
      setSelectedRoomId(0);
      fetchApprovals();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to create approval');
    }
  };

  const handleDelete = async (roomId: number) => {
    if (!confirm('승인 요청을 취소하시겠습니까?')) return;
    try {
      await roomApprovalApi.delete(roomId);
      fetchApprovals();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete approval');
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('ko-KR');
  };

  return (
    <div className="panel">
      <h2>Room Approval Management</h2>

      <div className="form">
        <div className="form-row">
          <select
            value={selectedRoomId}
            onChange={(e) => setSelectedRoomId(Number(e.target.value))}
          >
            <option value={0}>실 선택</option>
            {rooms.map(room => (
              <option key={room.id} value={room.id}>{room.name}</option>
            ))}
          </select>
          <button onClick={handleCreate} style={{ padding: '10px 20px', background: '#4a90d9', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
            승인 요청
          </button>
        </div>
      </div>

      <div className="filter-tabs" style={{ display: 'flex', gap: '8px', marginBottom: '16px' }}>
        <button
          onClick={() => setFilter('all')}
          style={{ padding: '8px 16px', background: filter === 'all' ? '#4a90d9' : '#e0e0e0', color: filter === 'all' ? 'white' : 'black', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          전체
        </button>
        <button
          onClick={() => setFilter('pending')}
          style={{ padding: '8px 16px', background: filter === 'pending' ? '#4a90d9' : '#e0e0e0', color: filter === 'pending' ? 'white' : 'black', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          대기중
        </button>
        <button
          onClick={() => setFilter('approved')}
          style={{ padding: '8px 16px', background: filter === 'approved' ? '#4a90d9' : '#e0e0e0', color: filter === 'approved' ? 'white' : 'black', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          승인됨
        </button>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>실</th>
              <th>상태</th>
              <th>승인 선생님</th>
              <th>승인 시간</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {approvals.map((approval) => (
              <tr key={approval.room.id}>
                <td>{approval.room.name}</td>
                <td>{approval.approved ? '승인됨' : '대기중'}</td>
                <td>{approval.approvedTeacher?.username || '-'}</td>
                <td>{formatDate(approval.approvedAt)}</td>
                <td>
                  <button onClick={() => handleDelete(approval.room.id)} className="danger">취소</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
