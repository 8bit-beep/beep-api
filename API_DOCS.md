# beep-api API 문서

> 프론트엔드 개발자용 API 명세서

**Base URL**: `https://api.beep.example.com` (환경별 변경)

**인증**: Bearer Token (JWT)
```
Authorization: Bearer {accessToken}
```

---

## 목차
1. [인증 (Auth)](#1-인증-auth)
2. [사용자 (User)](#2-사용자-user)
3. [학생 일정 (Schedule)](#3-학생-일정-schedule)
4. [실 (Room)](#4-실-room)
5. [실 승인 (Room Approval)](#5-실-승인-room-approval)
6. [체크포인트 (Checkpoint)](#6-체크포인트-checkpoint)
7. [출석 타입 (Attendance Type)](#7-출석-타입-attendance-type)
8. [출석 (Attendance)](#8-출석-attendance)
9. [실이동 (Shift)](#9-실이동-shift)
10. [장기결석 (Absence)](#10-장기결석-absence)
11. [메모 (Memo)](#11-메모-memo)
12. [알림 (Notification)](#12-알림-notification)
13. [교내 행사 (Event)](#13-교내-행사-event)
14. [큐빅 외박자 OpenAPI](#14-큐빅-외박자-openapi)

---

## 1. 인증 (Auth)

### 1.1 DAuth 로그인

| 항목 | 내용 |
|------|------|
| **URL** | `POST /dauth/login` |
| **설명** | DAuth OAuth 인증 코드로 로그인 |
| **인증** | 불필요 |

**Request Body**
```json
{
  "code": "dauth_authorization_code"
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 1.2 토큰 갱신

| 항목 | 내용 |
|------|------|
| **URL** | `POST /auth/refresh` |
| **설명** | Refresh Token으로 새로운 토큰 발급 |
| **인증** | 불필요 |

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 2. 사용자 (User)

### 2.1 내 정보 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /users/my` |
| **설명** | 현재 로그인한 사용자 정보 조회 |
| **인증** | 필요 |

**Response** `200 OK`
```json
{
  "id": 1,
  "email": "student@dsm.hs.kr",
  "username": "홍길동",
  "role": "STUDENT",
  "profileImage": "https://...",
  "studentInfo": {
    "grade": 2,
    "classNumber": 3,
    "num": 15
  },
  "currentStatus": {
    "id": 1,
    "name": "동아리"
  }
}
```

---

### 2.2 사용자 삭제

| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /users/{userId}` |
| **설명** | 사용자 계정 삭제 (Soft Delete) |
| **인증** | 필요 (ADMIN) |
| **Path Param** | `userId`: 삭제할 사용자 ID |

**Response** `200 OK`

---

### 2.3 학생 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /students` |
| **설명** | 학생 목록 조회 (필터/페이징) |
| **인증** | 필요 (TEACHER) |

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `grade` | Integer | X | 학년 (1, 2, 3) |
| `classNumber` | Integer | X | 반 (1, 2, 3, 4) |
| `keyword` | String | X | 이름 검색 |
| `page` | Integer | X | 페이지 번호 (기본: 0) |
| `size` | Integer | X | 페이지 크기 (기본: 20) |

**Response** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "email": "student@dsm.hs.kr",
      "username": "홍길동",
      "studentInfo": {
        "grade": 2,
        "classNumber": 3,
        "num": 15
      }
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

---

### 2.4 제한 사용자 관리

| URL | Method | 설명 |
|-----|--------|------|
| `/limited-users` | `POST` | 제한 사용자 등록 |
| `/limited-users` | `GET` | 제한 사용자 목록 |
| `/limited-users/{id}` | `PATCH` | 제한 사용자 수정 |
| `/limited-users/{id}` | `DELETE` | 제한 사용자 삭제 |

**POST Request Body**
```json
{
  "email": "blocked@dsm.hs.kr"
}
```

**GET Response** `200 OK`
```json
[
  {
    "id": 1,
    "email": "blocked@dsm.hs.kr"
  }
]
```

---

## 3. 학생 일정 (Schedule)

### 3.1 API 목록

| URL | Method | 설명 | 비고 |
|-----|--------|------|------|
| `/schedules` | `POST` | 학생 일정 생성 | 교사용 |
| `/schedules` | `GET` | 특정 학생 일정 조회 | `?userId=` 필수 |
| `/schedules/my` | `POST` | 내 일정 생성 | 학생용 |
| `/schedules/my` | `GET` | 내 일정 조회 | 학생용 |
| `/schedules/{scheduleId}` | `PATCH` | 일정 수정 | |
| `/schedules/{scheduleId}` | `DELETE` | 일정 삭제 | |

### 3.2 일정 생성 (교사용)

**POST /schedules Request Body**
```json
{
  "userId": 1,
  "dayOfWeek": "MONDAY",
  "checkpointId": 1,
  "typeId": 1,
  "roomId": 1
}
```

### 3.3 내 일정 생성 (학생용)

**POST /schedules/my Request Body**
```json
{
  "dayOfWeek": "MONDAY",
  "checkpointId": 1,
  "typeId": 1,
  "roomId": 1
}
```

**dayOfWeek 값**: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

### 3.4 일정 조회 Response

```json
[
  {
    "id": 1,
    "dayOfWeek": "MONDAY",
    "checkpoint": {
      "id": 1,
      "name": "1차 자습"
    },
    "type": {
      "id": 1,
      "name": "동아리"
    },
    "room": {
      "id": 1,
      "name": "동아리실 1"
    }
  }
]
```

---

## 4. 실 (Room)

### 4.1 API 목록

| URL | Method | 설명 |
|-----|--------|------|
| `/rooms` | `POST` | 실 생성 |
| `/rooms` | `GET` | 실 목록 조회 |
| `/rooms/{roomId}` | `GET` | 실 상세 조회 |
| `/rooms/{roomId}` | `PATCH` | 실 수정 |
| `/rooms/{roomId}` | `DELETE` | 실 삭제 |

### 4.2 실 생성/수정

**Request Body**
```json
{
  "name": "동아리실 1",
  "grade": 2,
  "classNumber": 3,
  "floor": 4
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 실 이름 (최대 100자) |
| `grade` | Integer | X | 학년 |
| `classNumber` | Integer | X | 반 |
| `floor` | Integer | X | 층 |

### 4.3 실 조회 Response

```json
{
  "id": 1,
  "name": "동아리실 1",
  "grade": 2,
  "classNumber": 3,
  "floor": 4
}
```

---

## 5. 실 승인 (Room Approval)

> 교사가 특정 체크포인트에 실을 점검 완료했음을 표시

### 5.1 API 목록

| URL | Method | 설명 |
|-----|--------|------|
| `/rooms/{roomId}/approvals` | `POST` | 실 승인 생성 |
| `/rooms/{roomId}/approvals` | `GET` | 특정 실 승인 상태 조회 |
| `/rooms/{roomId}/approvals` | `DELETE` | 실 승인 삭제 |
| `/approvals` | `GET` | 전체 승인 목록 조회 |

### 5.2 승인 목록 조회

**GET /approvals Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `approved` | Boolean | X | `true`: 승인됨, `false`: 미승인, 없음: 전체 |

**Response** `200 OK`
```json
[
  {
    "room": {
      "id": 1,
      "name": "동아리실 1"
    },
    "approved": true,
    "approvedTeacher": {
      "id": 1,
      "username": "김선생"
    },
    "approvedAt": "2024-01-15T19:30:00"
  }
]
```

---

## 6. 체크포인트 (Checkpoint)

> 출석 시간대 (예: 1차 자습, 2차 자습)

### 6.1 API 목록

| URL | Method | 설명 |
|-----|--------|------|
| `/checkpoints` | `POST` | 체크포인트 생성 |
| `/checkpoints` | `GET` | 체크포인트 목록 조회 |
| `/checkpoints/{checkpointId}` | `GET` | 체크포인트 상세 조회 |
| `/checkpoints/{checkpointId}` | `PATCH` | 체크포인트 수정 |
| `/checkpoints/{checkpointId}` | `DELETE` | 체크포인트 삭제 |

### 6.2 체크포인트 생성/수정

**Request Body**
```json
{
  "name": "1차 자습",
  "startAt": "19:00",
  "endAt": "21:00",
  "attendanceStartAt": "18:50",
  "attendanceEndAt": "19:10"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 체크포인트 이름 (최대 50자) |
| `startAt` | LocalTime | O | 시작 시간 (HH:mm) |
| `endAt` | LocalTime | O | 종료 시간 (HH:mm) |
| `attendanceStartAt` | LocalTime | O | 출석 가능 시작 시간 |
| `attendanceEndAt` | LocalTime | O | 출석 가능 종료 시간 |

### 6.3 체크포인트 조회 Response

```json
{
  "id": 1,
  "name": "1차 자습",
  "startAt": "19:00",
  "endAt": "21:00",
  "attendanceStartAt": "18:50",
  "attendanceEndAt": "19:10"
}
```

---

## 7. 출석 타입 (Attendance Type)

> 출석 상태 종류 (예: 동아리, 교실자습, 외박)

### 7.1 API 목록

| URL | Method | 설명 |
|-----|--------|------|
| `/types` | `POST` | 출석 타입 생성 |
| `/types` | `GET` | 출석 타입 목록 조회 |
| `/types/{id}` | `GET` | 출석 타입 상세 조회 |
| `/types/{id}` | `PUT` | 출석 타입 수정 |
| `/types/{id}` | `DELETE` | 출석 타입 삭제 |

### 7.2 출석 타입 생성/수정

**Request Body**
```json
{
  "name": "동아리"
}
```

### 7.3 출석 타입 조회 Response

```json
[
  { "id": 1, "name": "동아리" },
  { "id": 2, "name": "교실자습" },
  { "id": 3, "name": "외박" },
  { "id": 4, "name": "미출석" }
]
```

**시스템 예약 타입**
- `미출석`: 출석하지 않은 기본 상태
- `외박`: 장기결석 기본 타입

---

## 8. 출석 (Attendance)

### 8.1 API 목록

| URL | Method | 설명 | 대상 |
|-----|--------|------|------|
| `/attendances` | `POST` | 출석하기 | 학생 |
| `/attendances/cancel` | `PATCH` | 출석 취소 | 학생 |
| `/attendances` | `GET` | 출석 현황 조회 | 교사 |
| `/attendances/status` | `PATCH` | 출석 상태 변경 | 교사 |
| `/attendances/histories` | `GET` | 출석 히스토리 파일 목록 | 교사 |
| `/attendances/histories/download` | `GET` | 출석 히스토리 다운로드 | 교사 |

### 8.2 출석하기 (학생)

**POST /attendances Request Body**
```json
{
  "roomId": 1,
  "typeId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `roomId` | Long | O | 출석할 실 ID |
| `typeId` | Long | O | 출석 타입 ID |

**주의사항**
- 출석 가능 시간(`attendanceStartAt` ~ `attendanceEndAt`) 내에만 출석 가능
- 이미 출석한 경우 `ALREADY_ATTENDED` 에러
- 일정과 다른 타입/실로 출석 시 `TYPE_MISMATCH` / `ROOM_MISMATCH` 에러

---

### 8.3 출석 현황 조회 (교사)

**GET /attendances Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `roomId` | Long | X | - | 실 필터 |
| `statusId` | Long | X | - | 출석 타입 필터 |
| `grade` | Integer | X | - | 학년 필터 |
| `classNumber` | Integer | X | - | 반 필터 |
| `isCurrentCheckpoint` | Boolean | X | `true` | 현재 체크포인트만 조회 |
| `page` | Integer | X | 0 | 페이지 번호 |
| `size` | Integer | X | 20 | 페이지 크기 |

**Response** `200 OK`
```json
{
  "content": [
    {
      "userId": 1,
      "studentId": "2315",
      "username": "홍길동",
      "statuses": [
        {
          "checkpoint": { "id": 1, "name": "1차 자습" },
          "status": { "id": 1, "name": "동아리" }
        },
        {
          "checkpoint": { "id": 2, "name": "2차 자습" },
          "status": null
        }
      ]
    }
  ],
  "totalElements": 100,
  "totalPages": 5
}
```

---

### 8.4 출석 상태 변경 (교사)

**PATCH /attendances/status Request Body**
```json
{
  "userId": 1,
  "statusId": 2,
  "date": "2024-01-15",
  "checkpointId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `userId` | Long | O | 학생 ID |
| `statusId` | Long | O | 변경할 출석 타입 ID |
| `date` | LocalDate | X | 날짜 (기본: 오늘) |
| `checkpointId` | Long | X | 체크포인트 ID (기본: 현재) |

---

### 8.5 출석 히스토리 다운로드

**GET /attendances/histories/download Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `date` | String | O | 날짜 (파일명 형식) |

**Response** `200 OK`
```json
{
  "url": "https://s3.amazonaws.com/..."
}
```

---

## 9. 실이동 (Shift)

> 학생이 다른 실로 이동 신청

### 9.1 API 목록

| URL | Method | 설명 | 대상 |
|-----|--------|------|------|
| `/shifts` | `POST` | 실이동 신청 | 학생 |
| `/shifts/my` | `GET` | 내 실이동 목록 | 학생 |
| `/shifts/{shiftId}` | `PATCH` | 실이동 수정 | 학생 |
| `/shifts/{shiftId}` | `DELETE` | 실이동 삭제 | 학생 |
| `/shifts` | `GET` | 전체 실이동 목록 | 교사 |
| `/shifts/{shiftId}/status` | `PATCH` | 실이동 승인/거절 | 교사 |

### 9.2 실이동 신청 (학생)

**POST /shifts Request Body**
```json
{
  "roomId": 1,
  "reason": "동아리 활동 참여",
  "checkpointId": 1,
  "date": "2024-01-15"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `roomId` | Long | O | 이동할 실 ID |
| `reason` | String | O | 사유 (최대 300자) |
| `checkpointId` | Long | O | 체크포인트 ID |
| `date` | LocalDate | O | 날짜 (오늘 이후) |

---

### 9.3 실이동 조회 Response

```json
[
  {
    "id": 1,
    "user": {
      "id": 1,
      "username": "홍길동",
      "studentInfo": {
        "grade": 2,
        "classNumber": 3,
        "num": 15
      }
    },
    "room": {
      "id": 1,
      "name": "동아리실 1"
    },
    "checkpoint": {
      "id": 1,
      "name": "1차 자습"
    },
    "reason": "동아리 활동 참여",
    "status": "WAITING",
    "date": "2024-01-15"
  }
]
```

**status 값**
- `WAITING`: 대기중
- `APPROVED`: 승인됨
- `REJECTED`: 거절됨

---

### 9.4 실이동 승인/거절 (교사)

**PATCH /shifts/{shiftId}/status Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `status` | String | O | `APPROVED`, `REJECTED`, `WAITING` |

---

## 10. 장기결석 (Absence)

> 외박, 병결 등 장기 결석 관리

### 10.1 API 목록

| URL | Method | 설명 | 응답 |
|-----|--------|------|------|
| `/absences` | `POST` | 장기결석 생성 | 200 |
| `/absences` | `GET` | 장기결석 목록 조회 | 200 |
| `/absences/{absenceId}` | `PATCH` | 장기결석 수정 | 200 |
| `/absences/{absenceId}` | `DELETE` | 장기결석 삭제 | 204 |

### 10.2 장기결석 생성

**POST /absences Request Body**
```json
{
  "userIds": [1, 2, 3],
  "startDate": "2024-01-15",
  "endDate": "2024-01-17",
  "reason": "가정체험학습",
  "checkpoints": [
    { "checkpointId": 1, "typeId": 5 },
    { "checkpointId": 2, "typeId": 6 },
    { "checkpointId": 3 }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `userIds` | Long[] | O | 대상 학생 ID 목록 |
| `startDate` | LocalDate | O | 시작일 (오늘 이후) |
| `endDate` | LocalDate | O | 종료일 (시작일 이후) |
| `reason` | String | O | 사유 (최대 500자) |
| `checkpoints` | CheckpointSetting[] | X | 체크포인트별 타입 설정 (미지정 시 전체 체크포인트) |

**CheckpointSetting**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `checkpointId` | Long | O | 체크포인트 ID |
| `typeId` | Long | X | 해당 체크포인트의 출석 타입 (미지정 시 기본 결석 타입) |

**Response** `200 OK`
```json
{
  "absenceId": 1,
  "skippedUserIds": [3]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `absenceId` | Long | 생성된 결석 ID (모두 스킵된 경우 null) |
| `skippedUserIds` | Long[] | 중복으로 제외된 학생 ID 목록 |

---

### 10.3 장기결석 수정

**PATCH /absences/{absenceId} Request Body**

생성과 동일한 형식

**Response** `200 OK`
```json
{
  "absenceId": 1,
  "skippedUserIds": []
}
```

---

### 10.4 장기결석 조회 Response

```json
{
  "content": [
    {
      "absenceId": 1,
      "isGrouped": false,
      "targetStudents": [
        {
          "name": "홍길동",
          "info": { "grade": 2, "classNumber": 3, "num": 15 }
        }
      ],
      "startDate": "2024-01-15",
      "endDate": "2024-01-17",
      "reason": "가정체험학습",
      "checkpoints": [
        {
          "checkpoint": { "id": 1, "name": "1차 자습" },
          "type": { "id": 5, "name": "외출" }
        },
        {
          "checkpoint": { "id": 2, "name": "2차 자습" },
          "type": { "id": 6, "name": "조퇴" }
        }
      ]
    }
  ],
  "totalElements": 10,
  "totalPages": 1
}
```

---

## 11. 메모 (Memo)

> 학년별 교사 메모. 학년당 하나만 존재한다.

### 11.1 API 목록

| URL | Method | 설명 | 권한 |
|-----|--------|------|------|
| `/memos/{grade}` | `POST` | 메모 생성 (이미 있으면 내용 갱신) | TEACHER |
| `/memos/{grade}` | `GET` | 메모 조회 | TEACHER |
| `/memos/{grade}` | `PATCH` | 메모 수정 | TEACHER |

`grade`는 1~3.

### 11.2 자동 영역과 수기 영역

`content`는 두 부분이 합쳐진 값이다.

```
8월 26일                                    ← 자동 영역 시작
8~9교시 체육대회 (3명 참여) - 천준범
1101 김철수 / 1102 이영희 / 1103 박민수
                                            ← 빈 줄로 구분
내일 시험이니 조용히 시키기                 ← 수기 영역
```

- **자동 영역**은 [교내 행사](#13-교내-행사-event) 등록·수정·삭제 때마다 서버가 통째로 다시 만든다. 여기를 직접 고쳐 보내도 다음 행사 변경 때 덮어써진다.
- **수기 영역**은 교사가 쓴 내용이며 행사 변경에 영향받지 않는다.
- `PATCH`로 보낸 전체 텍스트에서 서버가 자동 영역을 떼어내고 나머지를 수기 영역으로 저장한다. **조회로 받은 `content`를 그대로 편집해서 보내면 된다.**

### 11.3 메모 생성/수정

**Request Body** (`POST`는 `content`, `PATCH`는 `newContent`)
```json
{
  "content": "내일 시험이니 조용히 시키기"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `content` / `newContent` | String | O | 메모 내용 (최대 30000자) |

### 11.4 메모 조회 Response

```json
{
  "grade": 1,
  "content": "8월 26일\n8~9교시 체육대회 (3명 참여) - 천준범\n1101 김철수\n\n내일 시험이니 조용히 시키기",
  "isRead": true
}
```

조회하면 `isRead`가 `true`로 바뀐다. 수정하면 다시 `false`가 된다.

---

## 12. 알림 (Notification)

### 12.1 FCM 토큰 저장

| 항목 | 내용 |
|------|------|
| **URL** | `POST /fcm` |
| **설명** | 푸시 알림용 FCM 토큰 저장 |
| **인증** | 필요 |

**Request Body**
```json
{
  "token": "fcm_device_token...",
  "device": "android"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `token` | String | O | FCM 디바이스 토큰 |
| `device` | String | O | 디바이스 종류 (android, ios, web) |

---

### 12.2 테스트 푸시 발송

| 항목 | 내용 |
|------|------|
| **URL** | `GET /fcm` |
| **설명** | 전체 사용자에게 테스트 푸시 알림 발송 |
| **인증** | 필요 |

**Response** `200 OK` (응답 본문 없음)

---

## 13. 교내 행사 (Event)

> 교내 행사로 자습에서 빠지는 학생을 지정한다. 등록하면 해당 학생 × 교시의 출석이 `교내 행사` 타입으로 미리 생성되고, 참여 학년의 [메모](#11-메모-memo) 자동 영역이 갱신된다.

**권한**: 전 엔드포인트 `TEACHER`

### 13.1 API 목록

| URL | Method | 설명 |
|-----|--------|------|
| `/events` | `POST` | 행사 등록 |
| `/events?date=` | `GET` | 날짜별 행사 목록 |
| `/events/{eventId}` | `GET` | 행사 상세 |
| `/events/{eventId}` | `PATCH` | 행사 수정 |
| `/events/{eventId}` | `DELETE` | 행사 삭제 |

### 13.2 행사 등록 / 수정

**Request Body** (`POST /events`, `PATCH /events/{eventId}` 동일)
```json
{
  "name": "체육대회",
  "date": "2026-08-26",
  "checkpointIds": [1, 2],
  "userIds": [11, 12, 13]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | O | 행사명 (최대 100자) |
| `date` | String | O | 행사 날짜 `yyyy-MM-dd` |
| `checkpointIds` | Long[] | O | 교시 ID 목록 (1개 이상). `GET /checkpoints`에서 조회 |
| `userIds` | Long[] | O | 참여 학생 ID 목록 (1개 이상). `GET /students`에서 조회 |

담당 교사는 **요청 토큰에서 자동으로 채워진다.** 요청 본문에 넣지 않는다.

수정은 기존 출석·참여자·교시를 모두 지우고 새 내용으로 다시 만든다. 이번 수정으로 빠지는 학년의 메모도 함께 갱신된다.

### 13.3 행사 목록 Response

`GET /events?date=2026-08-26` — `date`를 생략하면 오늘 날짜로 조회한다.

```json
[
  {
    "id": 3,
    "name": "체육대회",
    "date": "2026-08-26",
    "checkpointNames": ["8~9교시", "10~11교시"],
    "studentCount": 10,
    "createdByName": "천준범"
  }
]
```

`checkpointNames`는 교시 시작 시각 순으로 정렬된다.

### 13.4 행사 상세 Response

`GET /events/{eventId}` — 수정 화면을 채울 때 쓴다.

```json
{
  "id": 3,
  "name": "체육대회",
  "date": "2026-08-26",
  "checkpoints": [
    { "id": 1, "name": "8~9교시" },
    { "id": 2, "name": "10~11교시" }
  ],
  "students": [
    { "userId": 11, "studentId": "1101", "name": "김철수" },
    { "userId": 21, "studentId": "2101", "name": "이영희" }
  ],
  "createdByName": "천준범"
}
```

`students`는 학번 순으로 정렬된다.

### 13.5 에러

| 코드 | 상태 | 설명 |
|------|------|------|
| `EVENT_NOT_FOUND` | 404 | 행사를 찾을 수 없음 |
| `EMPTY_CHECKPOINTS` | 400 | 교시를 하나도 선택하지 않음 |
| `EMPTY_USERS` | 400 | 학생을 하나도 선택하지 않음 |
| `USER_NOT_FOUND` | 404 | 없는 학생 ID가 섞임 |
| `CHECKPOINT_NOT_FOUND` | 404 | 없는 교시 ID가 섞임 |

### 13.6 다른 기능에 미치는 영향

- 참여 학생의 출석 상태가 해당 교시에 `교내 행사`로 표시된다
- 출석 독촉 푸시 알림 대상에서 제외된다
- 실의 `currentStudentCount`에 포함되지 않는다
- 교사가 상태를 `미출석`으로 되돌려도 행사 출석 기록은 **삭제되지 않는다** (행사 목록과 어긋나지 않게 하기 위함)

---

## 14. 큐빅 외박자 OpenAPI

> 입력한 날짜에 외박 중인 학생 명단을 큐빅에 제공한다. 일반 사용자 JWT 대신 큐빅 전용 API 키를 사용한다.

### 14.1 외박자 명단 조회

| 항목 | 내용 |
|------|------|
| **Method** | `GET` |
| **URL** | `/out-sleeping/openapi/search` |
| **인증** | `X-Qvik-Api-Key` 헤더 |

**Request Header**

```http
X-Qvik-Api-Key: {qvikApiKey}
```

**Query Parameters**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `date` | String | O | 검색할 날짜 (`yyyy-MM-dd`) |

다음 조건 중 하나를 만족하는 학생을 포함한다.

- 조회일이 외박자 관리에 등록된 `외박` 기간에 포함되는 학생
- 조회일의 체크포인트 중 하나라도 출석 상태가 `외박`인 학생

같은 학생이 두 조건이나 여러 체크포인트에 중복되면 한 번만 반환한다. 결과는 학년, 반, 번호, 이름 순으로 정렬된다.

**Response** `200 OK`

```json
{
  "content": [
    {
      "publicId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "홍길동",
      "grade": 2,
      "room": 3,
      "number": 15
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `content` | Object[] | 외박 학생 목록. 결과가 없으면 빈 배열 |
| `publicId` | String? | 학생 DAuth 공개 식별자. 기존 데이터에 없으면 `null` |
| `name` | String | 학생 이름 |
| `grade` | Int | 학년 |
| `room` | Int | 반 |
| `number` | Int | 번호 |

### 14.2 에러

| 코드 | 상태 | 설명 |
|------|------|------|
| `DATE_REQUIRED` | 400 | `date` 파라미터 누락 |
| `METHOD_ARGUMENT_TYPE_MISMATCH` | 400 | 날짜 형식 오류 |
| `INVALID_QVIK_API_KEY` | 401 | API 키 누락 또는 불일치 |

운영 환경에는 `QVIK_API_KEY` 환경변수를 반드시 설정해야 한다. 누락하거나 공백으로 설정하면 애플리케이션이 시작되지 않는다.

---

## 에러 응답

### 공통 에러 형식

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "잘못된 요청입니다."
}
```

### 주요 에러 코드

| 코드 | 상태 | 설명 |
|------|------|------|
| `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없음 |
| `ROOM_NOT_FOUND` | 404 | 실을 찾을 수 없음 |
| `CHECKPOINT_NOT_FOUND` | 404 | 체크포인트를 찾을 수 없음 |
| `ATTENDANCE_NOT_FOUND` | 404 | 출석 기록을 찾을 수 없음 |
| `SHIFT_NOT_FOUND` | 404 | 실이동 신청을 찾을 수 없음 |
| `ABSENCE_NOT_FOUND` | 404 | 장기결석을 찾을 수 없음 |
| `ALREADY_ATTENDED` | 400 | 이미 출석 처리됨 |
| `TYPE_MISMATCH` | 400 | 일정과 출석 타입 불일치 |
| `ROOM_MISMATCH` | 400 | 일정과 실 불일치 |
| `CHECKPOINT_TIME_OVERLAP` | 409 | 체크포인트 시간 겹침 |
| `ROOM_ALREADY_EXISTS` | 409 | 이미 존재하는 실 이름 |
| `SHIFT_ALREADY_EXISTS` | 409 | 중복 실이동 신청 |
| `ALREADY_APPROVED` | 409 | 이미 승인됨 |
| `PASSED_TIME` | 400 | 이미 지난 시간 |
| `INVALID_DATE_RANGE` | 400 | 잘못된 날짜 범위 |
| `EXPIRED_TOKEN` | 401 | 만료된 토큰 |
| `INVALID_TOKEN` | 401 | 잘못된 토큰 |

---

## 부록: Enum 값

### UserRole
| 값 | 설명 |
|---|------|
| `STUDENT` | 학생 |
| `TEACHER` | 교사 |
| `ADMIN` | 관리자 |

### DayOfWeek
| 값 | 설명 |
|---|------|
| `MONDAY` | 월요일 |
| `TUESDAY` | 화요일 |
| `WEDNESDAY` | 수요일 |
| `THURSDAY` | 목요일 |
| `FRIDAY` | 금요일 |
| `SATURDAY` | 토요일 |
| `SUNDAY` | 일요일 |

### ShiftStatus
| 값 | 설명 |
|---|------|
| `WAITING` | 대기중 |
| `APPROVED` | 승인됨 |
| `REJECTED` | 거절됨 |
