import http from 'k6/http';
import { check, fail, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://host.docker.internal:8081').replace(/\/$/, '');
const MODE = __ENV.TEST_MODE || 'unknown';
const FEATURE = __ENV.FEATURE || 'log';
const RUN_ID = __ENV.RUN_ID || 'manual';

export const options = {
  stages: [
    { duration: '5s', target: 10 },
    { duration: '20s', target: 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
  tags: {
    test_mode: MODE,
    feature: FEATURE,
    run_id: RUN_ID,
  },
};

function login() {
  const response = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({
      companyCode: __ENV.COMPANY_CODE,
      memberNo: __ENV.MEMBER_NO,
      password: __ENV.PASSWORD,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'POST /api/v1/auth/login' },
    },
  );

  if (response.status !== 200) {
    fail(`Login failed: status=${response.status}, body=${response.body}`);
  }

  const accessToken = response.json('data.accessToken');
  if (!accessToken) {
    fail('Access token is missing');
  }
  return accessToken;
}

export function setup() {
  if (FEATURE === 'log') {
    return {};
  }
  return { accessToken: login() };
}

function testLog() {
  const response = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({
      companyCode: __ENV.COMPANY_CODE,
      memberNo: __ENV.MEMBER_NO,
      password: __ENV.PASSWORD,
    }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: '로그 생성 - 로그인' },
    },
  );
  check(response, { '로그 생성 API 응답 성공': (result) => result.status === 200 });
}

function testComment(accessToken) {
  const response = http.post(
    `${BASE_URL}/api/v1/tickets/${__ENV.TICKET_ID}/comments`,
    JSON.stringify({
      content: `[KAFKA-PERF:${MODE}] comment vu=${__VU} iter=${__ITER} at=${Date.now()}`,
    }),
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      tags: { name: '댓글 생성 및 WebSocket 이벤트' },
    },
  );
  check(response, { '댓글 생성 API 응답 성공': (result) => result.status === 200 });
}

function testNotification(accessToken) {
  const response = http.post(
    `${BASE_URL}/api/v1/tickets/asset-requests`,
    JSON.stringify({
      requestedUsageType: 'PERSONAL',
      assetType: 'TANGIBLE',
      assetItemId: __ENV.ASSET_ITEM_ID,
      quantity: 1,
      estimatedUnitPrice: 10000,
      requestReason: `[KAFKA-PERF:${MODE}] notification vu=${__VU} iter=${__ITER} at=${Date.now()}`,
      assignmentTargetMemberIds: [__ENV.MEMBER_ID],
    }),
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      tags: { name: '알림 생성 - 자산 요청 티켓' },
    },
  );
  check(response, { '알림 생성 API 응답 성공': (result) => result.status === 200 });
}

export default function (data) {
  if (FEATURE === 'log') {
    testLog();
  } else if (FEATURE === 'comment') {
    testComment(data.accessToken);
  } else if (FEATURE === 'notification') {
    testNotification(data.accessToken);
  } else {
    fail(`Unsupported FEATURE: ${FEATURE}`);
  }
  sleep(0.1);
}
