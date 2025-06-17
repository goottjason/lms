/**
 * 공통 API 호출
 * @param {"get"|"post"|"put"|"delete"} method
 * @param {string} url
 * @param {object} [data]  // POST/PUT 바디
 * @param {object} [params] // GET 쿼리스트링
 */
function apiCall(method, url, data = null, params = {}) {
  return axios({ method, url, data, params });
}

/**
 * URL 파라미터 유틸
 */
const UrlUtils = {
  /**
   * 쿼리스트링에서 값 꺼내기
   * @param {string} name 키 이름
   * @returns {string|null}
   */
  getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
  },

  /**
   * 경로 템플릿에 맞춰 세그먼트 파싱하기
   * @param {string} template e.g. '/test/testDetail/:testId/:foo'
   * @returns {Object} { testId: '8029', foo: '...' }
   */
  getPathParams(template) {
    const pathSegs     = window.location.pathname.split('/').filter(Boolean);
    const templateSegs = template.split('/').filter(Boolean);
    const params = {};
    templateSegs.forEach((seg, i) => {
      if (seg.startsWith(':')) {
        const key = seg.slice(1);
        params[key] = pathSegs[i] || null;
      }
    });
    return params;
  },

  /**
   * 경로 세그먼트 인덱스로 가져오기
   * @param {number} index 0부터 시작 (첫 번째 유효 세그먼트)
   * @returns {string|null}
   */
  getPathSegment(index) {
    const segs = window.location.pathname.split('/').filter(Boolean);
    return segs[index] || null;
  }
};
