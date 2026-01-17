import { useState, useEffect } from 'react';
import type { Student } from '../types';
import { studentApi } from '../api/client';

export function StudentPanel() {
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filters, setFilters] = useState({ grade: '', classNumber: '', keyword: '' });

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const params: { grade?: number; classNumber?: number; keyword?: string; page: number; size: number } = {
        page,
        size: 20,
      };
      if (filters.grade) params.grade = Number(filters.grade);
      if (filters.classNumber) params.classNumber = Number(filters.classNumber);
      if (filters.keyword) params.keyword = filters.keyword;

      const data = await studentApi.getAll(params);
      if (data) {
        setStudents(data.content);
        setTotalPages(data.totalPages);
      }
    } catch (e) {
      console.error('Failed to fetch students:', e);
      setStudents([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, [page]);

  const handleSearch = () => {
    setPage(0);
    fetchStudents();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  return (
    <div className="panel">
      <h2>Student Management</h2>

      <div className="form">
        <div className="form-row">
          <select
            value={filters.grade}
            onChange={(e) => setFilters({ ...filters, grade: e.target.value })}
          >
            <option value="">전체 학년</option>
            <option value="1">1학년</option>
            <option value="2">2학년</option>
            <option value="3">3학년</option>
          </select>
          <select
            value={filters.classNumber}
            onChange={(e) => setFilters({ ...filters, classNumber: e.target.value })}
          >
            <option value="">전체 반</option>
            {[1, 2, 3, 4].map(n => (
              <option key={n} value={n}>{n}반</option>
            ))}
          </select>
          <input
            type="text"
            placeholder="이름/이메일 검색"
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            onKeyDown={handleKeyDown}
          />
          <button
            onClick={handleSearch}
            style={{ padding: '10px 20px', background: '#4a90d9', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
          >
            검색
          </button>
        </div>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>이름</th>
                <th>이메일</th>
                <th>학년</th>
                <th>반</th>
                <th>번호</th>
              </tr>
            </thead>
            <tbody>
              {students.map((student) => (
                <tr key={student.id}>
                  <td>{student.id}</td>
                  <td>{student.username}</td>
                  <td>{student.email}</td>
                  <td>{student.grade}</td>
                  <td>{student.classNumber}</td>
                  <td>{student.num}</td>
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
