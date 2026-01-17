import { useState, useEffect } from 'react';
import type { Checkpoint, CreateCheckpointRequest, UpdateCheckpointRequest } from '../types';
import { checkpointApi } from '../api/client';

export function CheckpointPanel() {
  const [checkpoints, setCheckpoints] = useState<Checkpoint[]>([]);
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CreateCheckpointRequest>({
    name: '',
    startAt: '',
    endAt: '',
    attendanceStartAt: '',
    attendanceEndAt: '',
  });

  const fetchCheckpoints = async () => {
    setLoading(true);
    try {
      const data = await checkpointApi.getAll();
      setCheckpoints(data);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to fetch checkpoints');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCheckpoints();
  }, []);

  const resetForm = () => {
    setForm({
      name: '',
      startAt: '',
      endAt: '',
      attendanceStartAt: '',
      attendanceEndAt: '',
    });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        const updateData: UpdateCheckpointRequest = {};
        if (form.name) updateData.name = form.name;
        if (form.startAt) updateData.startAt = form.startAt;
        if (form.endAt) updateData.endAt = form.endAt;
        if (form.attendanceStartAt) updateData.attendanceStartAt = form.attendanceStartAt;
        if (form.attendanceEndAt) updateData.attendanceEndAt = form.attendanceEndAt;
        await checkpointApi.update(editingId, updateData);
      } else {
        await checkpointApi.create(form);
      }
      resetForm();
      fetchCheckpoints();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to save checkpoint');
    }
  };

  const handleEdit = (cp: Checkpoint) => {
    setEditingId(cp.id);
    setForm({
      name: cp.name,
      startAt: cp.startAt,
      endAt: cp.endAt,
      attendanceStartAt: cp.attendanceStartAt,
      attendanceEndAt: cp.attendanceEndAt,
    });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this checkpoint?')) return;
    try {
      await checkpointApi.delete(id);
      fetchCheckpoints();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete checkpoint');
    }
  };

  return (
    <div className="panel">
      <h2>Checkpoint Management</h2>

      <form onSubmit={handleSubmit} className="form">
        <div className="form-row">
          <input
            type="text"
            placeholder="Name *"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required={!editingId}
          />
          <input
            type="time"
            placeholder="Start At *"
            value={form.startAt}
            onChange={(e) => setForm({ ...form, startAt: e.target.value })}
            required={!editingId}
          />
          <input
            type="time"
            placeholder="End At *"
            value={form.endAt}
            onChange={(e) => setForm({ ...form, endAt: e.target.value })}
            required={!editingId}
          />
        </div>
        <div className="form-row">
          <input
            type="time"
            placeholder="Attendance Start *"
            value={form.attendanceStartAt}
            onChange={(e) => setForm({ ...form, attendanceStartAt: e.target.value })}
            required={!editingId}
          />
          <input
            type="time"
            placeholder="Attendance End *"
            value={form.attendanceEndAt}
            onChange={(e) => setForm({ ...form, attendanceEndAt: e.target.value })}
            required={!editingId}
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
              <th>Start</th>
              <th>End</th>
              <th>Att. Start</th>
              <th>Att. End</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {checkpoints.map((cp) => (
              <tr key={cp.id}>
                <td>{cp.id}</td>
                <td>{cp.name}</td>
                <td>{cp.startAt}</td>
                <td>{cp.endAt}</td>
                <td>{cp.attendanceStartAt}</td>
                <td>{cp.attendanceEndAt}</td>
                <td>
                  <button onClick={() => handleEdit(cp)}>Edit</button>
                  <button onClick={() => handleDelete(cp.id)} className="danger">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
