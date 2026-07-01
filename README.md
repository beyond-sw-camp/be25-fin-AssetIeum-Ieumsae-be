# 자산이음 (Asset Ieum)
<img width="1672" height="941" alt="자산이음" src="https://github.com/user-attachments/assets/e82d78ca-fde5-4258-8afd-9816be98a482" />

## 📍 프로젝트 소개
**자산이음(Asset-Ieum)** 은 기업 내 유·무형 자산의 전 생애주기를 통합 관리하는 **Enterprise Asset Management Platform**입니다.

기업 내 자산 요청, 승인, 구매, 지급, 반납, 회수 과정을 하나의 플랫폼에서 관리하며, 분산되어 관리되던 자산 정보를 통합하여 운영 효율성과 데이터 정합성을 향상시킵니다.

또한, **HR 시스템 연동, 실시간 알림, 자동화 배치 프로세스**를 통해 자산 라이프사이클 전 과정을 자동화하여 기업 운영 비용 절감과 업무 효율 극대화를 지원합니다.

### ✨ 핵심 특징

- 🏢 유·무형 자산 통합 관리
- 🔄 HR 연동 기반 자산 라이프사이클 자동화
- 🎫 티켓 기반 자산 운영 프로세스
- 🔔 SSE + Redis Pub/Sub 기반 실시간 알림
- ⚙️ Spring Batch 기반 운영 자동화
- 📊 데이터 기반 운영 대시보드
- 📝 감사 로그 및 운영 이력 관리

### 🌐 서비스 링크

| 구분 | 링크 |
|------|------|
| 🌍 Service | https://assetieum.com |

---

## 팀원 소개
<table align="center">
  <tr>
    <td align="center">
      <img src="images/레드.jpg" width="130" height="130" alt="양준석" /><br />
      <b>양준석</b><br />
      <a href="https://github.com/YJunSuk">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/블루.jpg" width="130" height="130" alt="모희주" /><br />
      <b>모희주</b><br />
      <a href="https://github.com/heejudy">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/핑크.jpg" width="130" height="130" alt="이애은" /><br />
      <b>이애은</b><br />
      <a href="https://github.com/nueeaeel">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
    <td align="center">
      <img src="images/옐로우.jpg" width="130" height="130" alt="이민경" /><br />
      <b>이민경</b><br />
      <a href="https://github.com/alskung1101">
      <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white"/>
      </a>
    </td>
  </tr>
</table>
<br>

## 목차

1. [📍 주요 기능](#1-주요-기능)
2. [🛠 기술 스택](#2-기술-스택)
3. [🏗 시스템 아키텍처](#3-시스템-아키텍처)
4. [📋 요구사항 명세서](#4-요구사항-명세서)
5. [🗂 테이블 명세서](#5-테이블-명세서)
6. [🧩 ERD](#6-erd)
7. [🎨 화면 기능 설계서](#7-화면-기능-설계서)
8. [🖥 상세 서비스 화면](#8-상세-서비스-화면)
9. [🧪 테스트 보고서](#9-테스트)
10. [🚀 CI/CD 계획서](#12-cicd-계획서)
11. [⚡ 성능 테스트](#13-성능-테스트)
12. [🔥 트러블 슈팅](#15-트러블-슈팅)
13. [📝 회고](#16-회고)

<br>

---

## 1. 주요 기능
| 아이콘 | 기능 | 설명 |
|:---:|---|---|
| 👥 | 조직 관리 | 회사 / 부서 / 사원 / 권한 관리 |
| 💼 | 자산 관리 | 유형·무형 자산 등록, 배정, 반납, 회수 및 이력 관리 |
| 🛒 | 구매 관리 | 구매 요청, 구매 계획, 구매 정책, 예산 및 집행 이력 관리 |
| 🎫 | 티켓 관리 | 표준 자산 요청, 비표준 구매 요청, 직접 구매, 대여, 유지보수, 반납/해지, 반품/환불 프로세스 관리 |
| 🔄 | HR 연동 자동화 | 입사·퇴사·부서 이동 이벤트 기반 자산 배정, 회수, 티켓 자동 처리 |
| 📋 | 전수조사 | QR 기반 자산 실사, 응답 수집, 미응답 알림 및 후속 조치 관리 |
| 🔔 | 알림 시스템 | SSE 기반 실시간 알림, 티켓 승인/반려/처리 이벤트 전파 |
| 📊 | 대시보드 | 자산 보유 현황, 만료 예정 자산, 티켓 진행률, 예산 현황, HR 이벤트 등 운영 KPI 제공 |
| 📝 | 감사 및 로그 | 사용자 활동 로그, 감사 추적, 주요 업무 변경 이력 관리 |



<br>

## 2. 기술 스택
### 🔧 Backend
<p>
  <img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/spring batch-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Data JPA-000000?style=for-the-badge&logo=apache&logoColor=white">
</p>

### 🗄️ Database
<p>
  <img src="https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=white">
  <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">
</p>

### 🚀 Infra
<p>
  <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/docker--compose-2496ED?style=for-the-badge&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/aws cloudfront-F38020?style=for-the-badge&logo=cloudflare&logoColor=white">
  <img src="https://img.shields.io/badge/aws-F38020?style=for-the-badge&logo=cloudflare&logoColor=white">

</p>

### ⚙️ CI/CD
<p>
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/github%20actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
</p>

### 🤝 Collaboration
<p>
  <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white">
  <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white">
  <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/erdcloud-0B4F6C?style=for-the-badge&logo=icloud&logoColor=white">
  <img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=white">
</p>
<br/>

<br>

## 3. 시스템 아키텍처
<img width="1639" height="743" alt="image" src="https://github.com/user-attachments/assets/eba26bed-48a7-41b4-a64a-fa4d701748e7" />



<br>

## 4. 요구사항 명세서

- 📋[ 요구사항 명세서 ](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=683579060#gid=683579060)


<br>

## 5. 테이블 명세서 
- 🗂[ 테이블 명세서 ](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=2140880264#gid=2140880264)



<br>

## 6. ERD
- [ERD Cloud](https://www.erdcloud.com/d/jHtNFPTxxwohPjgER)
<img width="4940" height="2772" alt="자산이음_ERD" src="https://github.com/user-attachments/assets/3d87a248-aa49-4d0a-835b-f6d44a3ca510" />

<br>

## 7. 화면 기능 설계서 
- [ 화면 기능 설계서 ](https://www.figma.com/design/i5o7Xeb6cELARDILAXW309/%EC%9E%90%EC%82%B0-%EC%9D%B4%EC%9D%8C?node-id=399-509&t=ruWNjERUCQMxksow-0)


<br>

## 8. 상세 서비스 화면 

---

<br>

## 9. 백엔드 단위 테스트
- [백엔드 단위테스트](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=481770483#gid=481770483)

<br>

## 10. 프론트엔드 단위 테스트
- [프론트엔드 단위테스트](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=324859763#gid=324859763)


<br>

## 11. 통합 테스트 결과서 
- [통합 테스트 결과서](docs/통합_테스트_결과서.pdf)

<br>

## 12. CI/CD 계획서 
- [CI/CD 계획서](docs/CICD_계획서.pdf)

<br>

## 13. 성능 테스트

---

<br>

## 14. 트러블 슈팅

---

<br>

## 15. 회고

<details>
<summary><b>양준석</b></summary>
<br>

</details>

---

<details>
<summary><b>모희주</b></summary>
<br>

</details>

---

<details>
<summary><b>이애은</b></summary>
<br>

</details>

---

<details>
<summary><b>이민경</b></summary>
<br>

</details>

---

<br>

## 10. 그 외 산출물

### 프로젝트 문서
- [WBS](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=0#gid=0)
- [기획서](./docs/자산이음%20기획서.pdf)
- [요구사항 명세서](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=683579060#gid=683579060)
- [테이블 명세서](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=2140880264#gid=2140880264)
- [와이어 프레임](https://www.figma.com/design/i5o7Xeb6cELARDILAXW309/%EC%9E%90%EC%82%B0-%EC%9D%B4%EC%9D%8C?node-id=399-509&t=ruWNjERUCQMxksow-0)

- [프로그램 사양서](https://rp3hgdqcg2.apidog.io/)
- [백엔드 단위테스트](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=481770483#gid=481770483)
- [프론트엔드 단위테스트](https://docs.google.com/spreadsheets/d/1z-TKzG4AP284DwpEWPyhNDyRp2_YIFeRTpRxHoSSBkk/edit?gid=324859763#gid=324859763)

- [통합 테스트 결과서](docs/통합_테스트_결과서.pdf)
- [CI/CD 계획서](docs/CICD_계획서.pdf)
### ERD
- [ERD Cloud](https://www.erdcloud.com/d/jHtNFPTxxwohPjgER)
<img width="4940" height="2772" alt="자산이음_ERD" src="https://github.com/user-attachments/assets/3d87a248-aa49-4d0a-835b-f6d44a3ca510" />
