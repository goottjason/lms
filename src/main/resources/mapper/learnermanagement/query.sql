-- learnersAllorOne (과정, 배정강사, 배정관리자, 배정강의실)

-- courseId로 과정 API 조회하여 담아두기
    -- DTO 내부에 과정진행률 추가 (course_schedule 테이블의 courseId & period=1 로 조회하여 현재날짜전/전체
-- leId로 participation + participation_reason (DTOS) / employment_support (DTO) 조회
    -- 출석 DTOS 내부에 status Count, attendanceRate 추가
-- courseId와 ulId로 homework + homework_submission + homework_eval (DTOS) / test + test_submission (DTOS) 조회

SELECT 
ul.id AS ulId, ul.fullname AS ulFullname, le.id AS leId, le.completion_status AS leStatus,
c.id AS cId, c.name AS cName,
es.employment_status AS esStatus,
sa.user_id AS saUserId, us.fullname AS usFullname, sd.position AS sdPosition,

cr.name AS crName

FROM user ul

LEFT JOIN learner_enrollment le ON ul.id = le.user_id
LEFT JOIN course c ON le.course_id = c.id

LEFT JOIN employment_support es ON le.id = es.learner_enrollment_id

LEFT JOIN staff_assignment sa ON c.id = sa.course_id
LEFT JOIN user us ON us.id = sa.user_id
LEFT JOIN staff_detail sd ON sd.user_id = us.id

LEFT JOIN classroom_allocation ca ON ca.course_id = c.id
LEFT JOIN classroom cr ON ca.classroom_id = cr.id

WHERE ul.type = 'LEARNER'; 
	-- ORDER BY sd.position DESC
    -- GROUP BY le.id
	-- AND us.id = 37 
    -- AND c.id = 38

