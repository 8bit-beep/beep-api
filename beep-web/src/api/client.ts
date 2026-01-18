let accessToken: string | null = localStorage.getItem('accessToken');

export const setAccessToken = (token: string | null) => {
  accessToken = token;
  if (token) {
    localStorage.setItem('accessToken', token);
  } else {
    localStorage.removeItem('accessToken');
  }
};

export const getAccessToken = () => accessToken;

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...options?.headers as Record<string, string>,
  };

  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    if (response.status === 401) {
      setAccessToken(null);
      window.location.reload();
    }
    const error = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : (undefined as T);
}

export const roomApi = {
  getAll: () => request<import('../types').Room[]>('/rooms'),
  create: (data: import('../types').CreateRoomRequest) =>
    request<import('../types').Room>('/rooms', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: import('../types').CreateRoomRequest) =>
    request<import('../types').Room>(`/rooms/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    request<void>(`/rooms/${id}`, { method: 'DELETE' }),
};

export const typeApi = {
  getAll: () => request<import('../types').AttendanceType[]>('/types'),
  create: (data: import('../types').CreateTypeRequest) =>
    request<import('../types').AttendanceType>('/types', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: import('../types').CreateTypeRequest) =>
    request<import('../types').AttendanceType>(`/types/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    request<void>(`/types/${id}`, { method: 'DELETE' }),
};

export const checkpointApi = {
  getAll: () => request<import('../types').Checkpoint[]>('/checkpoints'),
  create: (data: import('../types').CreateCheckpointRequest) =>
    request<void>('/checkpoints', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: import('../types').UpdateCheckpointRequest) =>
    request<void>(`/checkpoints/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    request<void>(`/checkpoints/${id}`, { method: 'DELETE' }),
};

export const scheduleApi = {
  getAll: () => request<import('../types').Schedule[]>('/schedules/my'),
  getByUserId: (userId: number) => request<import('../types').Schedule[]>(`/schedules?userId=${userId}`),
  create: (data: import('../types').CreateScheduleRequest) =>
    request<void>('/schedules', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  update: (id: number, data: import('../types').UpdateScheduleRequest) =>
    request<void>(`/schedules/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
  delete: (id: number) =>
    request<void>(`/schedules/${id}`, { method: 'DELETE' }),
};

export const authApi = {
  testLogin: (data: import('../types').TestLoginRequest) =>
    request<import('../types').TokenResponse>('/test/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
};

export const userApi = {
  getMe: () => request<import('../types').User>('/users/my'),
};

export const roomApprovalApi = {
  getAll: (approved?: boolean) => {
    const params = approved !== undefined ? `?approved=${approved}` : '';
    return request<import('../types').RoomApproval[]>(`/approvals${params}`);
  },
  create: (roomId: number) =>
    request<void>(`/rooms/${roomId}/approvals`, { method: 'POST' }),
  delete: (roomId: number) =>
    request<void>(`/rooms/${roomId}/approvals`, { method: 'DELETE' }),
};

export const studentApi = {
  getAll: (params?: { grade?: number; classNumber?: number; keyword?: string; page?: number; size?: number }) => {
    const searchParams = new URLSearchParams();
    if (params?.grade) searchParams.append('grade', String(params.grade));
    if (params?.classNumber) searchParams.append('classNumber', String(params.classNumber));
    if (params?.keyword) searchParams.append('keyword', params.keyword);
    if (params?.page !== undefined) searchParams.append('page', String(params.page));
    if (params?.size) searchParams.append('size', String(params.size));
    const query = searchParams.toString();
    return request<import('../types').Page<import('../types').Student>>(`/students${query ? `?${query}` : ''}`);
  },
};

export const attendanceApi = {
  getAll: (params?: import('../types').AttendanceSearchParams) => {
    const searchParams = new URLSearchParams();
    if (params?.roomId) searchParams.append('roomId', String(params.roomId));
    if (params?.statusId) searchParams.append('statusId', String(params.statusId));
    if (params?.grade) searchParams.append('grade', String(params.grade));
    if (params?.classNumber) searchParams.append('classNumber', String(params.classNumber));
    if (params?.isCurrentCheckpoint !== undefined) searchParams.append('isCurrentCheckpoint', String(params.isCurrentCheckpoint));
    if (params?.page !== undefined) searchParams.append('page', String(params.page));
    if (params?.size) searchParams.append('size', String(params.size));
    const query = searchParams.toString();
    return request<import('../types').Page<import('../types').AttendanceStudent>>(`/attendances${query ? `?${query}` : ''}`);
  },
  updateStatus: (data: import('../types').UpdateAttendanceStatusRequest) =>
    request<void>('/attendances/status', {
      method: 'PATCH',
      body: JSON.stringify(data),
    }),
};
