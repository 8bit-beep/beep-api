export interface Room {
  id: number;
  name: string;
  grade: number | null;
  classNumber: number | null;
  floor: number | null;
}

export interface CreateRoomRequest {
  name: string;
  grade?: number | null;
  classNumber?: number | null;
  floor?: number | null;
}

export interface AttendanceType {
  id: number;
  name: string;
}

export interface CreateTypeRequest {
  name: string;
}

export interface Checkpoint {
  id: number;
  name: string;
  startAt: string;
  endAt: string;
  attendanceStartAt: string;
  attendanceEndAt: string;
}

export interface CreateCheckpointRequest {
  name: string;
  startAt: string;
  endAt: string;
  attendanceStartAt: string;
  attendanceEndAt: string;
}

export interface UpdateCheckpointRequest {
  name?: string;
  startAt?: string;
  endAt?: string;
  attendanceStartAt?: string;
  attendanceEndAt?: string;
}

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface Schedule {
  id: number;
  dayOfWeek: DayOfWeek;
  checkpoint: Checkpoint;
  type: AttendanceType;
  room: Room;
}

export interface CreateScheduleRequest {
  dayOfWeek: DayOfWeek;
  checkpointId: number;
  typeId: number;
  roomId: number;
}

export interface UpdateScheduleRequest {
  dayOfWeek?: DayOfWeek;
  checkpointId?: number;
  typeId?: number;
  roomId?: number;
}

export interface TestLoginRequest {
  email: string;
  name: string;
  role: 'STUDENT' | 'TEACHER';
  grade?: number;
  classNumber?: number;
  num?: number;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface StudentInfo {
  id: number | null;
  grade: number;
  classNumber: number;
  num: number;
  cardId: string | null;
}

export interface User {
  id: number | null;
  email: string;
  username: string;
  role: 'STUDENT' | 'TEACHER';
  profileImage: string | null;
  studentInfo: StudentInfo | null;
  currentStatus: AttendanceType | null;
}

export interface ApprovedTeacher {
  id: number;
  username: string;
}

export interface RoomApproval {
  room: Room;
  approved: boolean;
  approvedTeacher: ApprovedTeacher | null;
  approvedAt: string | null;
}

export interface Student {
  id: number;
  username: string;
  email: string;
  grade: number;
  classNumber: number;
  num: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface StatusResponse {
  checkpoint: { id: number; name: string };
  status: AttendanceType | null;
}

export interface AttendanceStudent {
  userId: number;
  username: string;
  studentId: string;
  statuses: StatusResponse[];
}

export interface AttendanceSearchParams {
  roomId?: number;
  statusId?: number;
  grade?: number;
  classNumber?: number;
  page?: number;
  size?: number;
}

export interface UpdateAttendanceStatusRequest {
  userId: number;
  statusId: number;
  date?: string;
  checkpointId?: number;
}
