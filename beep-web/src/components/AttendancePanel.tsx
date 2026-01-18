import { useState, useEffect } from 'react';
import type { AttendanceStudent, AttendanceType, Room } from '../types';
import { attendanceApi, roomApi, typeApi } from '../api/client';

export function AttendancePanel() {
  const [attendances, setAttendances] = useState<AttendanceStudent[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [types, setTypes] = useState<AttendanceType[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({ grade: '', classNumber: '', roomId: '', statusId: '', isCurrentCheckpoint: 'true' });
  const [selectedStudent, setSelectedStudent] = useState<AttendanceStudent | null>(null);
  const [selectedCheckpointId, setSelectedCheckpointId] = useState<number | null>(null);
  const [selectedStatusId, setSelectedStatusId] = useState<string>('');

  useEffect(() => {
    roomApi.getAll().then(setRooms).catch(console.error);
    typeApi.getAll().then(setTypes).catch(console.error);
  }, []);

  const fetchAttendances = async () => {
    setLoading(true);
    try {
      const params: {
        roomId?: number;
        statusId?: number;
        grade?: number;
        classNumber?: number;
        isCurrentCheckpoint?: boolean;
        page: number;
        size: number;
      } = { page, size: 20 };
      if (filters.grade) params.grade = Number(filters.grade);
      if (filters.classNumber) params.classNumber = Number(filters.classNumber);
      if (filters.roomId) params.roomId = Number(filters.roomId);
      if (filters.statusId) params.statusId = Number(filters.statusId);
      if (filters.isCurrentCheckpoint) params.isCurrentCheckpoint = filters.isCurrentCheckpoint === 'true';

      const data = await attendanceApi.getAll(params);
      if (data) {
        setAttendances(data.content);
        setTotalPages(data.totalPages);
      }
    } catch (e) {
      console.error('Failed to fetch attendances:', e);
      setAttendances([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAttendances();
  }, [page]);

  const handleSearch = () => {
    setPage(0);
    fetchAttendances();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const handleStatusChange = async () => {
    if (!selectedStudent || selectedCheckpointId === null || !selectedStatusId) return;

    try {
      await attendanceApi.updateStatus({
        userId: selectedStudent.userId,
        statusId: Number(selectedStatusId),
        checkpointId: selectedCheckpointId,
      });
      setSelectedStudent(null);
      setSelectedCheckpointId(null);
      setSelectedStatusId('');
      fetchAttendances();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to update status');
    }
  };

  return (
    <div className="panel">
      <h2>Attendance Management</h2>

      <div className="form">
        <div className="form-row">
          <select value={filters.grade} onChange={(e) => setFilters({ ...filters, grade: e.target.value })}>
            <option value="">전체 학년</option>
            <option value="1">1학년</option>
            <option value="2">2학년</option>
            <option value="3">3학년</option>
          </select>
          <select value={filters.classNumber} onChange={(e) => setFilters({ ...filters, classNumber: e.target.value })}>
            <option value="">전체 반</option>
            {[1, 2, 3, 4].map(n => (
              <option key={n} value={n}>{n}반</option>
            ))}
          </select>
          <select value={filters.roomId} onChange={(e) => setFilters({ ...filters, roomId: e.target.value })}>
            <option value="">전체 실</option>
            {rooms.map(room => (
              <option key={room.id} value={room.id}>{room.name}</option>
            ))}
          </select>
          <select value={filters.statusId} onChange={(e) => setFilters({ ...filters, statusId: e.target.value })}>
            <option value="">전체 상태</option>
            {types.map(type => (
              <option key={type.id} value={type.id}>{type.name}</option>
            ))}
          </select>
          <select value={filters.isCurrentCheckpoint} onChange={(e) => setFilters({ ...filters, isCurrentCheckpoint: e.target.value })} onKeyDown={handleKeyDown}>
            <option value="true">현재 체크포인트</option>
            <option value="false">전체 체크포인트</option>
          </select>
          <button
            onClick={handleSearch}
            style={{ padding: '10px 20px', background: '#4a90d9', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
          >
            검색
          </button>
        </div>
      </div>

      {selectedStudent && (
        <div style={{ margin: '16px 0', padding: '16px', background: '#f5f5f5', borderRadius: '8px' }}>
          <h3>{selectedStudent.username} ({selectedStudent.studentId}) 출석 상태 변경</h3>
          <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
            <select
              value={selectedCheckpointId ?? ''}
              onChange={(e) => setSelectedCheckpointId(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">체크포인트 선택</option>
              {selectedStudent.statuses.map(s => (
                <option key={s.checkpoint.id} value={s.checkpoint.id}>{s.checkpoint.name}</option>
              ))}
            </select>
            <select value={selectedStatusId} onChange={(e) => setSelectedStatusId(e.target.value)}>
              <option value="">상태 선택</option>
              {types.map(type => (
                <option key={type.id} value={type.id}>{type.name}</option>
              ))}
            </select>
            <button
              onClick={handleStatusChange}
              disabled={selectedCheckpointId === null || !selectedStatusId}
              style={{ padding: '8px 16px', background: '#4a90d9', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
            >
              변경
            </button>
            <button
              onClick={() => { setSelectedStudent(null); setSelectedCheckpointId(null); setSelectedStatusId(''); }}
              style={{ padding: '8px 16px', background: '#999', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
            >
              취소
            </button>
          </div>
        </div>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table>
            <thead>
              <tr>
                <th>학번</th>
                <th>이름</th>
                <th>출석 상태</th>
                <th>액션</th>
              </tr>
            </thead>
            <tbody>
              {attendances.map((attendance) => (
                <tr key={attendance.studentId}>
                  <td>{attendance.studentId}</td>
                  <td>{attendance.username}</td>
                  <td>
                    {attendance.statuses.map(s => (
                      <span key={s.checkpoint.id} style={{ marginRight: '8px' }}>
                        {s.checkpoint.name}: {s.status?.name ?? '-'}
                      </span>
                    ))}
                  </td>
                  <td>
                    <button
                      onClick={() => setSelectedStudent(attendance)}
                      style={{ padding: '4px 8px', cursor: 'pointer' }}
                    >
                      상태 변경
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '16px' }}>
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              style={{ padding: '8px 16px', cursor: page === 0 ? 'not-allowed' : 'pointer' }}
            >
              이전
            </button>
            <span style={{ padding: '8px 16px' }}>
              {page + 1} / {totalPages || 1}
            </span>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={page >= totalPages - 1}
              style={{ padding: '8px 16px', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
            >
              다음
            </button>
          </div>
        </>
      )}
    </div>
  );
}
