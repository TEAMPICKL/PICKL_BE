## 멋사 중앙해커톤  
_2025/07/21 ~ 2025/08/25_

<br>

## 🎯 Git Convention

- 🎉 **Start:** Start New Project [:tada:]
- ✨ **Feat:** 새로운 기능을 추가 [:sparkles:]
- 🐛 **Fix:** 버그 수정 [:bug:]
- 🎨 **Design:** CSS 등 사용자 UI 디자인 변경 [:art:]
- ♻️ **Refactor:** 코드 리팩토링 [:recycle:]
- 🔧 **Settings:** Changing configuration files [:wrench:]
- 🗃️ **Comment:** 필요한 주석 추가 및 변경 [:card_file_box:]
- ➕ **Dependency/Plugin:** Add a dependency/plugin [:heavy_plus_sign:]
- 📝 **Docs:** 문서 수정 [:memo:]
- 🔀 **Merge:** Merge branches [:twisted_rightwards_arrows:]
- 🚀 **Deploy:** Deploying stuff [:rocket:]
- 🚚 **Rename:** 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 [:truck:]
- 🔥 **Remove:** 파일을 삭제하는 작업만 수행한 경우 [:fire:]
- ⏪️ **Revert:** 전 버전으로 롤백 [:rewind:]

<br>

## 📝 커밋 메시지 형식

형식: 작업 내용 요약

예:  
✨ Feat: 로그인 기능 구현  
🐛 Fix: 로그인 오류 해결  

<br>

## 🪴 Branch Convention (GitHub Flow)

- `main`: 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지  
- `develop`: 개발 중 사용할 브랜치  
- `feature/{description}`: 새로운 기능을 개발하는 브랜치  
  - 예: `feature/add-login-page`  

<br>

## Flow

1. `develop` 브랜치에서 `feature` 브랜치 생성.  
2. 작업을 완료하고 커밋 메시지에 맞게 커밋.  
3. Pull Request를 생성 / 팀원들의 리뷰.  
4. 리뷰가 완료 후 `develop` 브랜치로 병합.  
5. 배포 시점에 `develop` 브랜치를 `main` 브랜치로 병합.  
6. `main` 브랜치 배포  

**예시**:
```bash
# 새로운 기능 개발 브랜치 생성
git checkout -b feature/기능명

# 작업 후 커밋 & 원격 저장소에 푸시
git add .
git commit -m "✨ Feat: 기능 설명"
git push origin feature/기능명

# ➡️ GitHub에서 PR(Pull Request) 생성
#    base: develop ← compare: feature/기능명
#    팀원들과 코드 리뷰 후 develop 브랜치로 병합
