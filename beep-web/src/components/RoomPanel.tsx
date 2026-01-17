import { useState, useEffect } from 'react';
import type { Room, CreateRoomRequest } from '../types';
import { roomApi } from '../api/client';

export function RoomPanel() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CreateRoomRequest>({
    name: '',
    grade: null,
    classNumber: null,
    floor: null,
  });

  const fetchRooms = async () => {
    setLoading(true);
    try {
      const data = await roomApi.getAll();
      setRooms(data);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to fetch rooms');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, []);

  const resetForm = () => {
    setForm({ name: '', grade: null, classNumber: null, floor: null });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await roomApi.update(editingId, form);
      } else {
        await roomApi.create(form);
      }
      resetForm();
      fetchRooms();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to save room');
    }
  };

  const handleEdit = (room: Room) => {
    setEditingId(room.id);
    setForm({
      name: room.name,
      grade: room.grade,
      classNumber: room.classNumber,
      floor: room.floor,
    });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this room?')) return;
    try {
      await roomApi.delete(id);
      fetchRooms();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete room');
    }
  };

  return (
    <div className="panel">
      <h2>Room Management</h2>

      <form onSubmit={handleSubmit} className="form">
        <div className="form-row">
          <input
            type="text"
            placeholder="Name *"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
          <input
            type="number"
            placeholder="Grade"
            value={form.grade ?? ''}
            onChange={(e) => setForm({ ...form, grade: e.target.value ? Number(e.target.value) : null })}
          />
          <input
            type="number"
            placeholder="Class Number"
            value={form.classNumber ?? ''}
            onChange={(e) => setForm({ ...form, classNumber: e.target.value ? Number(e.target.value) : null })}
          />
          <input
            type="number"
            placeholder="Floor"
            value={form.floor ?? ''}
            onChange={(e) => setForm({ ...form, floor: e.target.value ? Number(e.target.value) : null })}
          />
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
              <th>Name</th>
              <th>Grade</th>
              <th>Class</th>
              <th>Floor</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rooms.map((room) => (
              <tr key={room.id}>
                <td>{room.id}</td>
                <td>{room.name}</td>
                <td>{room.grade ?? '-'}</td>
                <td>{room.classNumber ?? '-'}</td>
                <td>{room.floor ?? '-'}</td>
                <td>
                  <button onClick={() => handleEdit(room)}>Edit</button>
                  <button onClick={() => handleDelete(room.id)} className="danger">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
