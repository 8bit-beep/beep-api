import { useState, useEffect } from 'react';
import type { Schedule, CreateScheduleRequest, Room, AttendanceType, Checkpoint, DayOfWeek, Student } from '../types';
import { scheduleApi, roomApi, typeApi, checkpointApi, studentApi } from '../api/client';

const DAYS_OF_WEEK: { value: DayOfWeek; label: string }[] = [
  { value: 'MONDAY', label: '월요일' },
  { value: 'TUESDAY', label: '화요일' },
  { value: 'WEDNESDAY', label: '수요일' },
  { value: 'THURSDAY', label: '목요일' },
  { value: 'FRIDAY', label: '금요일' },
  { value: 'SATURDAY', label: '토요일' },
  { value: 'SUNDAY', label: '일요일' },
];

export function SchedulePanel() {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [types, setTypes] = useState<AttendanceType[]>([]);
  const [checkpoints, setCheckpoints] = useState<Checkpoint[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [studentSearch, setStudentSearch] = useState({ grade: '', classNumber: '', keyword: '' });
  const [form, setForm] = useState<CreateScheduleRequest>({
    userId: 0,
    dayOfWeek: 'MONDAY',
    checkpointId: 0,
    typeId: 0,
    roomId: 0,
  });

  const fetchSchedules = async (userId: number) => {
    setLoading(true);
    try {
      const data = await scheduleApi.getByUserId(userId);
      setSchedules(data);
    } catch (e) {
      console.error('Failed to fetch schedules:', e);
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchOptions = async () => {
    try {
      const [roomsData, typesData, checkpointsData] = await Promise.all([
        roomApi.getAll(),
        typeApi.getAll(),
        checkpointApi.getAll(),
      ]);
      setRooms(roomsData);
      setTypes(typesData);
      setCheckpoints(checkpointsData);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to fetch options');
    }
  };

  const searchStudents = async () => {
    try {
      const params: { grade?: number; classNumber?: number; keyword?: string; size: number } = { size: 50 };
      if (studentSearch.grade) params.grade = Number(studentSearch.grade);
      if (studentSearch.classNumber) params.classNumber = Number(studentSearch.classNumber);
      if (studentSearch.keyword) params.keyword = studentSearch.keyword;
      const data = await studentApi.getAll(params);
      setStudents(data.content);
    } catch (e) {
      console.error('Failed to search students:', e);
    }
  };

  useEffect(() => {
    fetchOptions();
  }, []);

  useEffect(() => {
    if (selectedUserId) {
      fetchSchedules(selectedUserId);
      setForm(f => ({ ...f, userId: selectedUserId }));
    } else {
      setSchedules([]);
    }
  }, [selectedUserId]);

  const resetForm = () => {
    setForm({
      userId: selectedUserId || 0,
      dayOfWeek: 'MONDAY',
      checkpointId: 0,
      typeId: 0,
      roomId: 0,
    });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.userId || !form.roomId || !form.typeId || !form.checkpointId) {
      alert('모든 필드를 선택해주세요');
      return;
    }
    try {
      if (editingId) {
        await scheduleApi.update(editingId, {
          dayOfWeek: form.dayOfWeek,
          checkpointId: form.checkpointId,
          typeId: form.typeId,
          roomId: form.roomId,
        });
      } else {
        await scheduleApi.create(form);
      }
      resetForm();
      if (selectedUserId) fetchSchedules(selectedUserId);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to save schedule');
    }
  };

  const handleEdit = (schedule: Schedule) => {
    setEditingId(schedule.id);
    setForm({
      userId: selectedUserId || 0,
      dayOfWeek: schedule.dayOfWeek,
      checkpointId: schedule.checkpoint.id,
      typeId: schedule.type.id,
      roomId: schedule.room.id,
    });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this schedule?')) return;
    try {
      await scheduleApi.delete(id);
      if (selectedUserId) fetchSchedules(selectedUserId);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete schedule');
    }
  };

  const getDayLabel = (day: DayOfWeek) => {
    return DAYS_OF_WEEK.find(d => d.value === day)?.label || day;
  };

  const handleStudentSelect = (student: Student) => {
    setSelectedUserId(student.id);
    setStudents([]);
  };

  return (
    <div className="panel">
      <h2>Schedule Management</h2>

      <div className="form" style={{ marginBottom: '20px', padding: '16px', background: '#f5f5f5', borderRadius: '8px' }}>
        <h3>학생 선택</h3>
        <div className="form-row">
          <select
            value={studentSearch.grade}
            onChange={(e) => setStudentSearch({ ...studentSearch, grade: e.target.value })}
          >
            <option value="">전체 학년</option>
            <option value="1">1학년</option>
            <option value="2">2학년</option>
            <option value="3">3학년</option>
          </select>
          <select
            value={studentSearch.classNumber}
            onChange={(e) => setStudentSearch({ ...studentSearch, classNumber: e.target.value })}
          >
            <option value="">전체 반</option>
            {[1, 2, 3, 4].map(n => (
              <option key={n} value={n}>{n}반</option>
            ))}
          </select>
          <input
            type="text"
            placeholder="이름 검색"
            value={studentSearch.keyword}
            onChange={(e) => setStudentSearch({ ...studentSearch, keyword: e.target.value })}
            onKeyDown={(e) => e.key === 'Enter' && searchStudents()}
          />
          <button type="button" onClick={searchStudents} style={{ padding: '10px 20px', background: '#4a90d9', color: 'white', border: 'none', borderRadius: '4px' }}>
            검색
          </button>
        </div>
        {students.length > 0 && (
          <div style={{ marginTop: '8px', maxHeight: '150px', overflowY: 'auto', border: '1px solid #ddd', borderRadius: '4px' }}>
            {students.map(student => (
              <div
                key={student.id}
                onClick={() => handleStudentSelect(student)}
                style={{ padding: '8px 12px', cursor: 'pointer', borderBottom: '1px solid #eee' }}
                onMouseOver={(e) => (e.currentTarget.style.background = '#e8f4ff')}
                onMouseOut={(e) => (e.currentTarget.style.background = 'white')}
              >
                {student.grade}-{student.classNumber} {String(student.num).padStart(2, '0')} {student.username}
              </div>
            ))}
          </div>
        )}
        {selectedUserId && (
          <div style={{ marginTop: '8px', padding: '8px', background: '#e8f4ff', borderRadius: '4px' }}>
            선택된 학생 ID: {selectedUserId}
            <button
              type="button"
              onClick={() => { setSelectedUserId(null); setSchedules([]); }}
              style={{ marginLeft: '8px', padding: '4px 8px', background: '#999', color: 'white', border: 'none', borderRadius: '4px' }}
            >
              선택 해제
            </button>
          </div>
        )}
      </div>

      {selectedUserId && (
        <form onSubmit={handleSubmit} className="form">
          <div className="form-row">
            <select
              value={form.dayOfWeek}
              onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value as DayOfWeek })}
              required
            >
              {DAYS_OF_WEEK.map(day => (
                <option key={day.value} value={day.value}>{day.label}</option>
              ))}
            </select>
            <select
              value={form.roomId}
              onChange={(e) => setForm({ ...form, roomId: Number(e.target.value) })}
              required
            >
              <option value={0}>실 선택</option>
              {rooms.map(room => (
                <option key={room.id} value={room.id}>{room.name}</option>
              ))}
            </select>
            <select
              value={form.typeId}
              onChange={(e) => setForm({ ...form, typeId: Number(e.target.value) })}
              required
            >
              <option value={0}>타입 선택</option>
              {types.map(type => (
                <option key={type.id} value={type.id}>{type.name}</option>
              ))}
            </select>
            <select
              value={form.checkpointId}
              onChange={(e) => setForm({ ...form, checkpointId: Number(e.target.value) })}
              required
            >
              <option value={0}>체크포인트 선택</option>
              {checkpoints.map(cp => (
                <option key={cp.id} value={cp.id}>{cp.name}</option>
              ))}
            </select>
          </div>
          <div className="form-actions">
            <button type="submit">{editingId ? 'Update' : 'Create'}</button>
            {editingId && <button type="button" onClick={resetForm}>Cancel</button>}
          </div>
        </form>
      )}

      {!selectedUserId ? (
        <p>학생을 선택해주세요</p>
      ) : loading ? (
        <p>Loading...</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>요일</th>
              <th>실</th>
              <th>타입</th>
              <th>체크포인트</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {schedules.map((schedule) => (
              <tr key={schedule.id}>
                <td>{schedule.id}</td>
                <td>{getDayLabel(schedule.dayOfWeek)}</td>
                <td>{schedule.room.name}</td>
                <td>{schedule.type.name}</td>
                <td>{schedule.checkpoint.name}</td>
                <td>
                  <button onClick={() => handleEdit(schedule)}>Edit</button>
                  <button onClick={() => handleDelete(schedule.id)} className="danger">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
