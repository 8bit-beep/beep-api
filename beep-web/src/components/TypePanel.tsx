import { useState, useEffect } from 'react';
import type { AttendanceType, CreateTypeRequest } from '../types';
import { typeApi } from '../api/client';

export function TypePanel() {
  const [types, setTypes] = useState<AttendanceType[]>([]);
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CreateTypeRequest>({ name: '' });

  const fetchTypes = async () => {
    setLoading(true);
    try {
      const data = await typeApi.getAll();
      setTypes(data);
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to fetch types');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTypes();
  }, []);

  const resetForm = () => {
    setForm({ name: '' });
    setEditingId(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await typeApi.update(editingId, form);
      } else {
        await typeApi.create(form);
      }
      resetForm();
      fetchTypes();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to save type');
    }
  };

  const handleEdit = (type: AttendanceType) => {
    setEditingId(type.id);
    setForm({ name: type.name });
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Delete this type?')) return;
    try {
      await typeApi.delete(id);
      fetchTypes();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to delete type');
    }
  };

  return (
    <div className="panel">
      <h2>Attendance Type Management</h2>

      <form onSubmit={handleSubmit} className="form">
        <div className="form-row">
          <input
            type="text"
            placeholder="Name *"
            value={form.name}
            onChange={(e) => setForm({ name: e.target.value })}
            required
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
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {types.map((type) => (
              <tr key={type.id}>
                <td>{type.id}</td>
                <td>{type.name}</td>
                <td>
                  <button onClick={() => handleEdit(type)}>Edit</button>
                  <button onClick={() => handleDelete(type.id)} className="danger">Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
