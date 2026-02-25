import { FormEvent, useEffect, useMemo, useState } from 'react';

type Course = {
  code: string;
  name: string;
  capacity: number;
  enrolledCount: number;
  remaining: number;
};

type EnrollmentResponse = {
  enrollmentId: string;
  status: 'CONFIRMED' | 'REJECTED';
  studentNo: string;
  courseCode: string;
  message: string;
};

type StudentEnrollment = {
  id: string;
  courseCode: string;
  status: 'CONFIRMED' | 'REJECTED';
  createdAt: string;
};

type AuditEntry = {
  id: string;
  eventType: string;
  entityType: string;
  entityId: string;
  payload: string;
  createdAt: string;
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8088';

function App() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [studentNo, setStudentNo] = useState('20201234');
  const [courseCode, setCourseCode] = useState('');
  const [result, setResult] = useState<EnrollmentResponse | null>(null);
  const [resultError, setResultError] = useState<string>('');

  const [searchStudentNo, setSearchStudentNo] = useState('20201234');
  const [studentEnrollments, setStudentEnrollments] = useState<StudentEnrollment[]>([]);

  const [auditEntries, setAuditEntries] = useState<AuditEntry[]>([]);
  const [expandedAuditId, setExpandedAuditId] = useState<string | null>(null);

  const selectedCourseCode = useMemo(() => {
    if (courseCode) return courseCode;
    return courses[0]?.code || '';
  }, [courseCode, courses]);

  useEffect(() => {
    refreshCourses();
    loadAudit();
  }, []);

  const refreshCourses = async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/courses`);
    if (!response.ok) {
      throw new Error('Could not load courses');
    }
    const data: Course[] = await response.json();
    setCourses(data);
    if (!courseCode && data.length > 0) {
      setCourseCode(data[0].code);
    }
  };

  const loadAudit = async () => {
    const response = await fetch(`${API_BASE_URL}/api/v1/audit?limit=50`);
    if (!response.ok) {
      throw new Error('Could not load audit entries');
    }
    const data: AuditEntry[] = await response.json();
    setAuditEntries(data);
  };

  const submitEnrollment = async (event: FormEvent) => {
    event.preventDefault();
    setResult(null);
    setResultError('');

    const response = await fetch(`${API_BASE_URL}/api/v1/enrollments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ studentNo, courseCode: selectedCourseCode })
    });

    const body = await response.json();

    if (response.status === 201 || response.status === 409) {
      setResult(body as EnrollmentResponse);
      await Promise.all([refreshCourses(), loadAudit()]);
      return;
    }

    setResultError(body.message || 'Enrollment request failed');
  };

  const loadStudentEnrollments = async () => {
    const response = await fetch(
      `${API_BASE_URL}/api/v1/enrollments?studentNo=${encodeURIComponent(searchStudentNo)}`
    );

    if (!response.ok) {
      setStudentEnrollments([]);
      return;
    }

    const data: StudentEnrollment[] = await response.json();
    setStudentEnrollments(data);
  };

  return (
    <main className="container">
      <h1>CampusFlow Monolith</h1>

      <section className="card">
        <div className="card-header">
          <h2>Courses</h2>
          <button onClick={refreshCourses}>Refresh</button>
        </div>
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Capacity</th>
              <th>Enrolled</th>
              <th>Remaining</th>
            </tr>
          </thead>
          <tbody>
            {courses.map((course) => (
              <tr key={course.code}>
                <td>{course.code}</td>
                <td>{course.name}</td>
                <td>{course.capacity}</td>
                <td>{course.enrolledCount}</td>
                <td>{course.remaining}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card">
        <h2>Enrollment Form</h2>
        <form onSubmit={submitEnrollment} className="form-grid">
          <label>
            Student No
            <input value={studentNo} onChange={(e) => setStudentNo(e.target.value)} required />
          </label>

          <label>
            Course
            <select value={selectedCourseCode} onChange={(e) => setCourseCode(e.target.value)}>
              {courses.map((course) => (
                <option key={course.code} value={course.code}>
                  {course.code} - {course.name}
                </option>
              ))}
            </select>
          </label>

          <button type="submit">Submit Enrollment</button>
        </form>

        {result && (
          <p className={result.status === 'CONFIRMED' ? 'banner success' : 'banner warning'}>
            {result.status} - {result.message} (id: {result.enrollmentId})
          </p>
        )}

        {resultError && <p className="banner warning">{resultError}</p>}
      </section>

      <section className="card">
        <h2>Student Enrollments</h2>
        <div className="row">
          <input
            value={searchStudentNo}
            onChange={(e) => setSearchStudentNo(e.target.value)}
            placeholder="Student no"
          />
          <button onClick={loadStudentEnrollments}>Load</button>
        </div>

        <table>
          <thead>
            <tr>
              <th>Enrollment ID</th>
              <th>Course</th>
              <th>Status</th>
              <th>Created At</th>
            </tr>
          </thead>
          <tbody>
            {studentEnrollments.map((enrollment) => (
              <tr key={enrollment.id}>
                <td>{enrollment.id}</td>
                <td>{enrollment.courseCode}</td>
                <td>{enrollment.status}</td>
                <td>{new Date(enrollment.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="card">
        <div className="card-header">
          <h2>Audit Feed (Latest 50)</h2>
          <button onClick={loadAudit}>Refresh</button>
        </div>

        <ul className="audit-list">
          {auditEntries.map((entry) => (
            <li key={entry.id} className="audit-item">
              <button
                className="audit-button"
                onClick={() => setExpandedAuditId(expandedAuditId === entry.id ? null : entry.id)}
              >
                {new Date(entry.createdAt).toLocaleString()} | {entry.eventType} | {entry.entityType} |{' '}
                {entry.entityId}
              </button>
              {expandedAuditId === entry.id && <pre>{entry.payload}</pre>}
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}

export default App;
