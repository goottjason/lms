/**
 * 시험 기본 정보 모델
 */
class TestInfo {
    constructor(
        title = "", startDate = "", endDate = "", testTime = 10,
        totalScore                                         = 1
    ) {
        console.log("[TestInfo] constructor");
        this.title      = title;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.testTime   = testTime;
        this.totalScore = totalScore;
    }

    setTitle(newTitle) {
        console.log("[TestInfo] setTitle:", {old: this.title, new: newTitle});
        this.title = newTitle;
    }

    setStartDate(newStart) {
        console.log("[TestInfo] setStartDate:",
                    {old: this.startDate, new: newStart});
        this.startDate = newStart;
    }

    setEndDate(newEnd) {
        console.log("[TestInfo] setEndDate:", {old: this.endDate, new: newEnd});
        this.endDate = newEnd;
    }

    setTestTime(newTime) {
        console.log("[TestInfo] setTestTime:",
                    {old: this.testTime, new: newTime});
        this.testTime = newTime;
    }

    setTotalScore(newScore) {
        console.log("[TestInfo] setTotalScore:",
                    {old: this.totalScore, new: newScore});
        this.totalScore = newScore;
    }

    clone() {
        const copy      = new TestInfo();
        copy.title      = this.title;
        copy.startDate  = this.startDate;
        copy.endDate    = this.endDate;
        copy.testTime   = this.testTime;
        copy.totalScore = this.totalScore;

        return copy;
    }

}

/**
 * 시험(퀴즈) 전체 모델
 */
class Quiz {
    constructor() {
        console.log("[Quiz] constructor");
        this.questions = [];
    }

    /**
     * 문항 추가 (최소 1개, 최대 15개)
     */
    addQuestion(arg) {
        console.log("[Quiz] addQuestion before:",
                    this.questions.map(q => q.questionNo));

        if (arg instanceof Question) {

            this.questions.push(arg);

        } else {

            if (this.questions.length >= 15) {
                console.warn("[Quiz] addQuestion skipped (limit reached):",
                             this.questions.length);
                return;
            }
            const no = this.questions.length + 1;
            this.questions.push(new Question(no, arg));

        }

        console.log("[Quiz] addQuestion after:",
                    this.questions.map(q => q.questionNo));
    }

    /**
     * 특정 문항 삭제 (최소 1개 유지)
     * @param {number} questionNo
     */
    removeQuestion(questionNo) {
        console.log("[Quiz] removeQuestion before:",
                    this.questions.map(q => q.questionNo));

        if (this.questions.length <= 1) {
            // 문항이 1개 이하의 경우에는 삭제 불가
            console.warn("[Quiz] removeQuestion skipped (minimum reached):",
                         this.questions.length);
            return;
        }

        // 해당 문항 번호의 question의 index
        const idx = this.questions.findIndex(q => q.questionNo === questionNo);
        if (idx === -1) {
            console.warn("[Quiz] removeQuestion skipped (not found):",
                         questionNo);
            return;
        }
        // 해당 index question 삭제
        this.questions.splice(idx, 1);
        // 문항 번호 정렬
        this._reindexQuestions();

        console.log("[Quiz] removeQuestion after:",
                    this.questions.map(q => q.questionNo));
    }

    _reindexQuestions() {
        this.questions.forEach((q, idx) => q.questionNo = idx + 1);

        console.log("[Quiz] _reindexQuestions:",
                    this.questions.map(q => q.questionNo));
    }

    get totalScore() {
        // questions 배열 순회하면서 배점 누적값 (총 배점) 반환
        const total = this.questions.reduce((sum, q) => sum + q.score, 0);
        console.log("[Quiz] totalScore:", total);
        return total;
    }

    clone() {
        const copy     = new Quiz();
        copy.questions = this.questions.map(q => q.clone());

        return copy;
    }

}

/**
 * 문항 모델
 */
class Question {
    constructor(questionNo, type = "MULTIPLE", title = "", score = 0) {
        console.log("[Question] constructor:",
                    {questionNo, type, title, score});
        this.questionNo = questionNo;
        this.type       = type;
        this.title      = title;
        this.score      = score;
        this.options    = [];
        this.answer     = type === "SHORT" ? "" : null;

        if (type === "MULTIPLE") {
            this.options = [new Option(1), new Option(2)];
            console.log("[Question] 초기 options:",
                        this.options.map(o => o.optionNo));
        }
    }

    addOption() {
        console.log("[Question] addOption before:",
                    this.options.map(o => o.optionNo));

        if (this.type !== "MULTIPLE" || this.options.length >= 4) {
            console.warn("[Question] addOption skipped (type or limit):",
                         {type: this.type, count: this.options.length});
            return;
        }

        const newNo = this.options.length + 1;
        this.options.push(new Option(newNo));

        console.log("[Question] addOption after:",
                    this.options.map(o => o.optionNo));
    }

    removeOption(optionNo) {
        console.log("[Question] removeOption before:",
                    this.options.map(o => o.optionNo));

        if (this.type !== "MULTIPLE" || this.options.length <= 2) {
            console.warn("[Question] removeOption skipped (type or limit):",
                         {type: this.type, count: this.options.length});

            return;
        }

        let idx;
        if (optionNo == null) {
            // 인자가 없는 경우 마지막 옵션 삭제
            idx = this.options.length - 1;
        } else {
            idx = this.options.findIndex(o => o.optionNo === optionNo);
            if (idx === -1) {
                console.warn(
                    `[Question] removeOption: optionNo ${optionNo} not found.`);
                return;
            }
        }

        this.options.splice(idx, 1);
        this._reindexOptions();

        console.log("[Question] removeOption after:",
                    this.options.map(o => o.optionNo));
    }

    _reindexOptions() {
        this.options.forEach((opt, idx) => opt.optionNo = idx + 1);
        console.log("[Question] _reindexOptions:",
                    this.options.map(o => o.optionNo));
    }

    setType(newType) {
        console.log("[Question] setType:", {old: this.type, new: newType});
        this.type = newType;
    }

    setCorrectOption(optionNo) {
        console.log("[Question] setCorrectOption:", optionNo);
        if (this.type !== "MULTIPLE") {
            return;
        }
        this.options.forEach(
            opt => opt.isCorrect = (opt.optionNo === optionNo));
        console.log("[Question] correct flags:",
                    this.options.map(o => o.isCorrect));
    }

    setShortAnswer(text) {
        console.log("[Question] setShortAnswer:", text);
        if (this.type !== "SHORT") {
            return;
        }
        this.answer = text;
    }

    setScore(newScore) {
        console.log("[Question] setScore:", {old: this.score, new: newScore});
        this.score = newScore;
    }

    clone() {
        const copy = new Question(this.questionNo, this.type, this.title,
                                  this.score);

        copy.options = this.options.map(o => o.clone());
        copy.answer  = this.answer;
        return copy;
    }
}

/**
 * 선택지 모델
 */
class Option {
    constructor(optionNo, content = "", isCorrect = false) {
        console.log("[Option] constructor:", {optionNo, content, isCorrect});
        this.optionNo  = optionNo;
        this.content   = content;
        this.isCorrect = isCorrect;
    }

    setContent(text) {
        console.log("[Option] setContent:", {old: this.content, new: text});
        this.content = text;
    }

    setCorrect(flag) {
        console.log("[Option] setCorrect:",
                    {optionNo: this.optionNo, old: this.isCorrect, new: flag});
        this.isCorrect = flag;
    }

    clone() {
        const copy     = new Option(this.optionNo);
        copy.content   = this.content;
        copy.isCorrect = this.isCorrect;
        return copy;
    }
}

// 디버깅용 전역 할당
window.TestInfo = TestInfo;
window.Quiz     = Quiz;
window.Question = Question;
window.Option   = Option;
