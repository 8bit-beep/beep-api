import { useState, useEffect } from 'react';
import type { Schedule, CreateScheduleRequest, Room, AttendanceType, Checkpoint, DayOfWeek } from '../types';
import { scheduleApi, roomApi, typeApi, checkpointApi } from '../api/client';

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
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CreateScheduleRequest>({
    dayOfWeek: 'MONDAY',
    checkpointId: 0,
    typeId: 0,
    roomId: 0,
  });

  const fetchSchedules = async () => {
    setLoading(true);
    try {
      const data = await scheduleApi.getAll();
      setSchedules(data);
    } catch (e) {
      console.error('Failed to fetch schedules:', e);
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

  useEffect(() => {
    fetchSchedules();
    fetchOptions();
  }, []);

  const resetForm = () => {
    setForm({
      dayOfWeek: 'MONDAY',
      checkpointId: 0,
      typeId: 0,
      roomId: 0,
    });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.roomId || !form.typeId || !form.checkpointId) {
      alert('모든 필드를 선택해주세요');
      return;
    }
    try {
      if (editingId) {
        await scheduleApi.update(editingId, form);
      } else {
        await scheduleApi.create(form);
      }
      resetForm();
      fetchSchedules();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to save schedule');
    }
  };

  const handleEdit = (schedule: Schedule) => {
    setEditingId(schedule.id);
    setForm({
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
      fetchSchedules();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete schedule');
    }
  };

  const getDayLabel = (day: DayOfWeek) => {
    return DAYS_OF_WEEK.find(d => d.value === day)?.label || day;
  };

  return (
    <div className="panel">
      <h2>Schedule Management</h2>

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

      {loading ? (
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
