import { useState, useEffect } from 'react';
import { RoomPanel } from './components/RoomPanel';
import { TypePanel } from './components/TypePanel';
import { CheckpointPanel } from './components/CheckpointPanel';
import { SchedulePanel } from './components/SchedulePanel';
import { RoomApprovalPanel } from './components/RoomApprovalPanel';
import { StudentPanel } from './components/StudentPanel';
import { AttendancePanel } from './components/AttendancePanel';
import { authApi, userApi, setAccessToken, getAccessToken } from './api/client';
import type { User } from './types';
import './App.css';

type Tab = 'room' | 'type' | 'checkpoint' | 'schedule' | 'approval' | 'student' | 'attendance';

function App() {
  const [activeTab, setActiveTab] = useState<Tab>('room');
  const [isLoggedIn, setIsLoggedIn] = useState(!!getAccessToken());
  const [user, setUser] = useState<User | null>(null);
  const [loginForm, setLoginForm] = useState({ email: '', name: '' });

  const fetchMe = async () => {
    try {
      const data = await userApi.getMe();
      setUser(data);
    } catch {
      setUser(null);
    }
  };

  useEffect(() => {
    if (isLoggedIn) {
      fetchMe();
    }
  }, [isLoggedIn]);

  const handleLogin = async () => {
    try {
      const res = await authApi.testLogin({
        email: loginForm.email,
        name: loginForm.name,
        role: 'STUDENT',
        grade: 1,
        classNumber: 1,
        num: 1,
      });
      setAccessToken(res.accessToken);
      setIsLoggedIn(true);
      setLoginForm({ email: '', name: '' });
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Login failed');
    }
  };

  const handleLogout = () => {
    setAccessToken(null);
    setIsLoggedIn(false);
    setUser(null);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Beep API Test</h1>
        <div className="login-area">
          {isLoggedIn ? (
            <>
              {user && <span className="user-info">{user.username} ({user.email})</span>}
              <button onClick={handleLogout} className="logout-btn">Logout</button>
            </>
          ) : (
            <div className="login-form">
              <input
                type="email"
                placeholder="Email"
                value={loginForm.email}
                onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
              />
              <input
                type="text"
                placeholder="Name"
                value={loginForm.name}
                onChange={(e) => setLoginForm({ ...loginForm, name: e.target.value })}
              />
              <button onClick={handleLogin}>Login</button>
            </div>
          )}
        </div>
      </header>

      <nav className="tabs">
        <button
          className={activeTab === 'room' ? 'active' : ''}
          onClick={() => setActiveTab('room')}
        >
          Room
        </button>
        <button
          className={activeTab === 'type' ? 'active' : ''}
          onClick={() => setActiveTab('type')}
        >
          Type
        </button>
        <button
          className={activeTab === 'checkpoint' ? 'active' : ''}
          onClick={() => setActiveTab('checkpoint')}
        >
          Checkpoint
        </button>
        <button
          className={activeTab === 'schedule' ? 'active' : ''}
          onClick={() => setActiveTab('schedule')}
        >
          Schedule
        </button>
        <button
          className={activeTab === 'approval' ? 'active' : ''}
          onClick={() => setActiveTab('approval')}
        >
          Approval
        </button>
        <button
          className={activeTab === 'student' ? 'active' : ''}
          onClick={() => setActiveTab('student')}
        >
          Student
        </button>
        <button
          className={activeTab === 'attendance' ? 'active' : ''}
          onClick={() => setActiveTab('attendance')}
        >
          Attendance
        </button>
      </nav>

      <main>
        {activeTab === 'room' && <RoomPanel />}
        {activeTab === 'type' && <TypePanel />}
        {activeTab === 'checkpoint' && <CheckpointPanel />}
        {activeTab === 'schedule' && <SchedulePanel />}
        {activeTab === 'approval' && <RoomApprovalPanel />}
        {activeTab === 'student' && <StudentPanel />}
        {activeTab === 'attendance' && <AttendancePanel />}
      </main>
    </div>
  );
}

export default App;
