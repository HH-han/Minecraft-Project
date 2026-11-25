<template>
  <div class="app-page">
    <div class="shell">
      <!-- 用户信息区域 -->
      <header class="profile-hero reveal">
        <div class="hero-glow"></div>
        <div class="hero-ring"></div>
        <div class="profile-main">
          <div class="avatar">
            <img
              :src="userInfo?.avatar || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20profile%20picture&image_size=square'"
              :alt="userInfo?.username || '用户头像'">
          </div>
          <div class="user-details">
            <div class="user-name-row">
              <h2>{{ userInfo?.username || t('profile.guestName') }}</h2>
              <span v-if="userInfo" class="member-tag">{{ t('profile.signedIn') }}</span>
            </div>
            <p class="user-sign">{{ userInfo?.signature || t('profile.defaultSign') }}</p>
            <button v-if="!userInfo" class="login-cta" @click="navigateTo('/pages/login/index')">
              {{ t('profile.signIn') }}
              <uni-icons type="right" size="13" color="#FFFFFF" />
            </button>
          </div>
        </div>
      </header>

      <!-- 我的服务 -->
      <div class="section-head">
        <div class="section-title">{{ t('profile.services') }}</div>
        <span class="section-sub">{{ t('profile.servicesSub') }}</span>
      </div>
      <div class="panel">
      <div class="menu-item" @click="navigateTo('/pages/order/index')">
        <div class="menu-icon">
          <svg t="1776003895239" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="12904" width="256" height="256">
            <path
              d="M372.7872 93.184m50.944 0l168.7552 0q50.944 0 50.944 50.944l0-0.0512q0 50.944-50.944 50.944l-168.7552 0q-50.944 0-50.944-50.944l0 0.0512q0-50.944 50.944-50.944Z"
              fill="#F67271" p-id="12905"></path>
            <path
              d="M785.1008 150.2208h-83.3536v10.24A82.3808 82.3808 0 0 1 619.52 242.688H403.712a82.432 82.432 0 0 1-82.3808-82.3808v-10.24H240.64A100.608 100.608 0 0 0 139.9296 250.88v578.56A100.608 100.608 0 0 0 240.64 929.8944h544.4608A100.608 100.608 0 0 0 885.76 829.44V250.88a100.608 100.608 0 0 0-100.6592-100.6592z m-76.8 583.68H317.44a25.6 25.6 0 0 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z m0-153.6H317.44a25.6 25.6 0 0 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z m0-153.6H317.44a25.6 25.6 0 1 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z"
              fill="#F5504E" p-id="12906"></path>
            <path
              d="M885.76 250.88a100.608 100.608 0 0 0-100.608-100.608h-83.4048v10.24A82.3808 82.3808 0 0 1 619.52 242.688H403.712a82.432 82.432 0 0 1-82.3808-82.3808v-10.24H240.64A100.608 100.608 0 0 0 139.9296 250.88v521.728A491.52 491.52 0 0 0 885.76 564.1728z m-177.3568 483.1232H317.44a25.6 25.6 0 0 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z m0-153.6H317.44a25.6 25.6 0 0 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z m0-153.6H317.44a25.6 25.6 0 1 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z"
              fill="#FF5A5A" p-id="12907"></path>
            <path
              d="M835.9424 191.3344c0-9.3696-0.512-18.688-1.1776-27.904a100.0448 100.0448 0 0 0-49.664-13.2096h-83.3536v10.24A82.3808 82.3808 0 0 1 619.52 242.688H403.712a82.432 82.432 0 0 1-82.3808-82.3808v-10.24H240.64A100.608 100.608 0 0 0 139.9296 250.88v394.24A489.3696 489.3696 0 0 0 302.08 687.9232a25.6 25.6 0 0 1 15.36-5.12h109.9776a488.8064 488.8064 0 0 0 225.9968-102.4H317.44a25.6 25.6 0 0 1 0-51.2h390.4a489.5744 489.5744 0 0 0 128.1024-337.8688z m-127.5904 235.776H317.44a25.6 25.6 0 1 1 0-51.2h391.0656a25.6 25.6 0 0 1 0 51.2z"
              fill="#F66A68" p-id="12908"></path>
            <path
              d="M244.3264 522.9056a489.0624 489.0624 0 0 0 284.7744-95.7952H317.44a25.6 25.6 0 1 1 0-51.2h270.7456a490.9056 490.9056 0 0 0 126.464-225.6896h-12.7488v10.24A82.3808 82.3808 0 0 1 619.52 242.688H403.712a82.432 82.432 0 0 1-82.3808-82.3808v-10.24H240.64A100.608 100.608 0 0 0 139.9296 250.88v262.4512a494.2848 494.2848 0 0 0 104.3968 9.5744z"
              fill="#F67271" p-id="12909"></path>
            <path
              d="M403.712 242.688a82.432 82.432 0 0 1-82.3808-82.3808v-10.24H240.64A100.608 100.608 0 0 0 139.9296 250.88v98.4576c8.7552 0.4096 17.5104 0.5632 26.368 0.4608A446.6688 446.6688 0 0 0 450.8672 242.688zM565.3504 93.184H423.68a50.944 50.944 0 0 0 0 101.8368h75.1616a448.5632 448.5632 0 0 0 66.5088-101.8368z"
              fill="#F67B7A" p-id="12910"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.order') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/collection/index')">
        <div class="menu-icon">
          <svg t="1776003921083" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="14769" width="256" height="256">
            <path d="M510.3616 505.9584m-450.816 0a450.816 450.816 0 1 0 901.632 0 450.816 450.816 0 1 0-901.632 0Z"
              fill="#FFE3DB" p-id="14770"></path>
            <path
              d="M826.7776 459.776a49.5104 49.5104 0 0 0-27.4432-84.4288l-142.2848-20.48a49.6128 49.6128 0 0 1-37.2736-27.1872l-63.6416-129.024a49.5104 49.5104 0 0 0-88.7808 0L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l102.9632 100.352a49.5104 49.5104 0 0 1 14.2336 44.032l-24.32 141.7216a49.5104 49.5104 0 0 0 71.68 52.1728l127.232-66.8672a49.5616 49.5616 0 0 1 46.08 0l127.232 66.8672a49.5104 49.5104 0 0 0 71.68-52.1728L709.5808 604.16a49.5104 49.5104 0 0 1 14.2336-43.8272z m-134.5024-8.9088a31.6928 31.6928 0 0 1-36.352 26.112l-74.9056-12.288a52.3264 52.3264 0 0 1-35.84-23.808l-38.656-61.7472a31.6928 31.6928 0 0 1 53.7088-33.5872l35.84 57.4976L666.1632 414.72a31.744 31.744 0 0 1 26.112 36.1472z"
              fill="#F23D4F" p-id="14771"></path>
            <path
              d="M290.7648 738.9696a397.0048 397.0048 0 0 0 476.16-368.64l-109.6704-15.9232a49.6128 49.6128 0 0 1-37.4784-26.7264l-63.6416-129.024a49.5104 49.5104 0 0 0-88.7808 0L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l102.9632 100.352a49.5104 49.5104 0 0 1 14.2336 44.032z m225.8432-403.456a31.5904 31.5904 0 0 1 43.6224 10.24l35.84 57.4976L666.1632 414.72a31.6928 31.6928 0 0 1-10.24 62.5152l-74.9056-12.288a52.3264 52.3264 0 0 1-35.84-23.808l-38.656-62.0032a31.6416 31.6416 0 0 1 10.0864-43.6224z"
              fill="#FC4956" p-id="14772"></path>
            <path
              d="M568.7296 460.8a52.2752 52.2752 0 0 1-23.552-20.1216l-38.656-61.5424a31.6928 31.6928 0 0 1 53.7088-33.5872l35.84 57.4976 10.9568 1.792a394.24 394.24 0 0 0 27.8528-59.392 48.896 48.896 0 0 1-15.104-17.7664l-63.6416-129.024a49.5104 49.5104 0 0 0-88.7808 0L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l102.9632 100.352a49.5616 49.5616 0 0 1 14.3872 42.496A396.2368 396.2368 0 0 0 568.7296 460.8z"
              fill="#FF5C64" p-id="14773"></path>
            <path
              d="M542.72 181.7088a49.6128 49.6128 0 0 0-75.264 16.9472L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l4.1472 4.0448A396.9024 396.9024 0 0 0 542.72 181.7088z"
              fill="#FF716E" p-id="14774"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.collection') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/travelNote/mine')">
        <div class="menu-icon">
          <svg t="1776003921083" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="14769" width="256" height="256">
            <path d="M510.3616 505.9584m-450.816 0a450.816 450.816 0 1 0 901.632 0 450.816 450.816 0 1 0-901.632 0Z"
              fill="#E7EADF" p-id="14770"></path>
            <path
              d="M826.7776 459.776a49.5104 49.5104 0 0 0-27.4432-84.4288l-142.2848-20.48a49.6128 49.6128 0 0 1-37.2736-27.1872l-63.6416-129.024a49.5104 49.5104 0 0 0-88.7808 0L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l102.9632 100.352a49.5104 49.5104 0 0 1 14.2336 44.032l-24.32 141.7216a49.5104 49.5104 0 0 0 71.68 52.1728l127.232-66.8672a49.5616 49.5616 0 0 1 46.08 0l127.232 66.8672a49.5104 49.5104 0 0 0 71.68-52.1728L709.5808 604.16a49.5104 49.5104 0 0 1 14.2336-43.8272z"
              fill="#1E5B48" p-id="14771"></path>
            <path
              d="M542.72 181.7088a49.6128 49.6128 0 0 0-75.264 16.9472L403.712 327.68a49.6128 49.6128 0 0 1-37.2736 27.0848l-142.2848 20.48a49.5104 49.5104 0 0 0-27.4432 84.5312l4.1472 4.0448A396.9024 396.9024 0 0 0 542.72 181.7088z"
              fill="#4F8A74" p-id="14774"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.notes') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/comment/index')">
        <div class="menu-icon">
          <svg t="1776003948215" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="16644" width="256" height="256">
            <path
              d="M345 641.8l27.3-18.1-0.1-0.1 36.1-24.1c15.4 3.2 31.4 5.1 48 5.1 112.6 0 204-78.9 204-176.1 0-97.3-91.3-176.1-204-176.1-112.6 0-204 78.9-204 176.1 0 61.8 36.6 116.1 92.6 147.5v65.8z m231.9-241.1c15.4 0 27.8 12.5 27.8 27.8 0 15.4-12.5 27.8-27.8 27.8-15.4 0-27.8-12.5-27.8-27.8s12.4-27.8 27.8-27.8z m-135.7 13.8c5.1-8.7 14.4-13.9 24.5-13.8 15.2 0.2 27.4 12.6 27.4 27.8 0 15.2-12.2 27.6-27.4 27.8-10 0.2-19.4-5.1-24.5-13.8-5.1-8.6-5.1-19.3 0-28z m-114.6 14.1c0-15.4 12.5-27.8 27.8-27.8 15.4 0 27.8 12.5 27.8 27.8 0 15.4-12.5 27.8-27.8 27.8-15.4 0-27.8-12.5-27.8-27.8z"
              fill="#A8CD53" p-id="16645"></path>
            <path
              d="M512 67C266.2 67 67 266.2 67 512s199.2 445 445 445 445-199.2 445-445S757.8 67 512 67zM215.3 426.2c0-116.5 107.9-210.9 241-210.9s241 94.4 241 210.9-107.9 210.9-241 210.9c-15.2 0-30-1.4-44.4-3.7l-104 64V592.2c-56.9-38.6-92.6-98.5-92.6-166zM715 703.5v105.2l-103.5-64c-14.7 2.4-29.3 3.7-44.2 3.7-71.4 0-135.2-27.3-179.4-70.5 7.3 0.4 14.6 0.9 21.9 0.9 10.2 0 20.5-0.5 30.7-1.3 34.8 24 78.9 38.4 126.9 38.4 16.5 0 32.6-1.9 48-5.1l36.1 24.1-0.5 0.1 26.8 18v-65.7c56-31.4 93.1-85.7 93.1-147.5 0-39.1-14.7-75.1-39.7-104.3 1.7-11.4 2.9-22.9 2.9-34.7 0-5.4-0.5-10.7-0.9-16 46.2 38.4 74.7 92.5 74.7 152.7 0.3 67.5-35.9 127.4-92.9 166z"
              fill="#A8CD53" p-id="16646"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.comments') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/points/index')">
        <div class="menu-icon">
          <svg t="1776003948215" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="16644" width="256" height="256">
            <path
              d="M512 51.2c-256 0-460.8 204.8-460.8 460.8s204.8 460.8 460.8 460.8 460.8-204.8 460.8-460.8-204.8-460.8-460.8-460.8zm0 870.4c-225.28 0-409.6-184.32-409.6-409.6s184.32-409.6 409.6-409.6 409.6 184.32 409.6 409.6-184.32 409.6-409.6 409.6z"
              fill="#C08A2E" p-id="16645"></path>
            <path
              d="M512 307.2c-35.84 0-64 28.16-64 64v153.6c0 35.84 28.16 64 64 64s64-28.16 64-64V371.2c0-35.84-28.16-64-64-64z"
              fill="#C08A2E" p-id="16646"></path>
            <path
              d="M512 128c-21.76 0-38.4 16.64-38.4 38.4v57.6c0 21.76 16.64 38.4 38.4 38.4s38.4-16.64 38.4-38.4v-57.6c0-21.76-16.64-38.4-38.4-38.4z"
              fill="#C08A2E" p-id="16647"></path>
            <path
              d="M512 832c-21.76 0-38.4 16.64-38.4 38.4v57.6c0 21.76 16.64 38.4 38.4 38.4s38.4-16.64 38.4-38.4v-57.6c0-21.76-16.64-38.4-38.4-38.4z"
              fill="#C08A2E" p-id="16648"></path>
            <path
              d="M128 512c0-21.76-16.64-38.4-38.4-38.4h-57.6c-21.76 0-38.4 16.64-38.4 38.4s16.64 38.4 38.4 38.4h57.6c21.76 0 38.4-16.64 38.4-38.4z"
              fill="#C08A2E" p-id="16649"></path>
            <path
              d="M956.8 512c0-21.76-16.64-38.4-38.4-38.4h-57.6c-21.76 0-38.4 16.64-38.4 38.4s16.64 38.4 38.4 38.4h57.6c21.76 0 38.4-16.64 38.4-38.4z"
              fill="#C08A2E" p-id="16650"></path>
            <path
              d="M264.704 264.704c-14.08 0-25.6 11.52-25.6 25.6v92.16c0 14.08 11.52 25.6 25.6 25.6s25.6-11.52 25.6-25.6v-92.16c0-14.08-11.52-25.6-25.6-25.6z"
              fill="#C08A2E" p-id="16651"></path>
            <path
              d="M806.656 264.704c-14.08 0-25.6 11.52-25.6 25.6v92.16c0 14.08 11.52 25.6 25.6 25.6s25.6-11.52 25.6-25.6v-92.16c0-14.08-11.52-25.6-25.6-25.6z"
              fill="#C08A2E" p-id="16652"></path>
            <path
              d="M264.704 806.656c-14.08 0-25.6 11.52-25.6 25.6v92.16c0 14.08 11.52 25.6 25.6 25.6s25.6-11.52 25.6-25.6v-92.16c0-14.08-11.52-25.6-25.6-25.6z"
              fill="#C08A2E" p-id="16653"></path>
            <path
              d="M806.656 806.656c-14.08 0-25.6 11.52-25.6 25.6v92.16c0 14.08 11.52 25.6 25.6 25.6s25.6-11.52 25.6-25.6v-92.16c0-14.08-11.52-25.6-25.6-25.6z"
              fill="#C08A2E" p-id="16654"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.points') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/chat/index')">
        <div class="menu-icon">
          <svg t="1776003974556" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="19477" width="256" height="256">
            <path
              d="M512.02 1013.551a498.239 498.239 0 0 1-354.672-146.889A498.239 498.239 0 0 1 10.48 512.01a498.302 498.302 0 0 1 146.91-354.693A498.239 498.239 0 0 1 512.02 10.47a498.134 498.134 0 0 1 354.611 146.888 498.448 498.448 0 0 1 146.93 354.652 498.302 498.302 0 0 1-146.909 354.632 498.176 498.176 0 0 1-354.631 146.91zM311.886 282.597a80.936 80.936 0 0 0-80.852 80.853v262.807a80.936 80.936 0 0 0 80.852 80.832h34.878l63.027 75.503a30.49 30.49 0 0 0 21.441 8.881 30.49 30.49 0 0 0 21.44-8.881l62.965-75.503h196.437a80.936 80.936 0 0 0 80.832-80.832V363.45a80.936 80.936 0 0 0-80.832-80.853z"
              fill="#2F7A55" p-id="19478"></path>
            <path
              d="M613.395 461.606H380.262a26.707 26.707 0 0 1 0-53.394h233.133a26.707 26.707 0 0 1 0 53.394z m-97.884 123.17H380.262a26.707 26.707 0 0 1 0-53.394h135.249a26.707 26.707 0 0 1 0 53.394z m0 0"
              fill="#2F7A55" p-id="19479"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.support') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
    </div>

      <!-- 设置区域 -->
      <div class="section-head">
        <div class="section-title">{{ t('profile.settingsMore') }}</div>
      </div>
      <div class="panel">
      <div class="menu-item" @click="navigateTo('/pages/login/scan')">
        <div class="menu-icon">
          <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="256" height="256">
            <path
              d="M117.368 487.445h371.92v-371.92h-371.92v371.92z m92.981-278.94h185.96v185.96h-185.96v-185.96z m325.43-92.98v371.92h371.92v-371.92H535.78z m278.94 278.94h-185.96v-185.96h185.96v185.96z m-557.88-46.49h92.98v-92.98h-92.98v92.98z m-139.47 557.88h371.92v-371.92h-371.92v371.92z m92.98-278.94h185.96v185.96h-185.96v-185.96z m557.88-371.92h-92.98v92.98h92.98v-92.98z m-232.45 650.86h92.98v-92.98h-92.98v92.98z m92.98-278.94v185.96h92.98v-185.96h-92.98z m185.96 185.96h-92.98v92.98h185.96v-185.96h-92.98v92.98z m0-185.96h92.98v-92.98h-92.98v92.98z m-278.94-92.98v92.98h92.98v-92.98h-92.98z m-278.94 232.45h92.98v-92.98h-92.98v92.98z"
              fill="#1E5B48"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.scan') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/profile/settings')">
        <div class="menu-icon">
          <svg t="1776004014552" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="21474" width="256" height="256">
            <path
              d="M512 512m-98.133333 0a98.133333 98.133333 0 1 0 196.266666 0 98.133333 98.133333 0 1 0-196.266666 0Z"
              fill="#8C948B" p-id="21475"></path>
            <path
              d="M891.733333 418.133333H853.333333c-42.666667 0-64-51.2-34.133333-81.066666l25.6-25.6c17.066667-17.066667 17.066667-46.933333 0-68.266667l-68.266667-68.266667c-17.066667-17.066667-46.933333-17.066667-68.266666 0l-25.6 25.6c-29.866667 29.866667-81.066667 8.533333-81.066667-34.133333v-34.133333c0-25.6-21.333333-46.933333-46.933333-46.933334h-93.866667c-25.6 0-46.933333 21.333333-46.933333 46.933334V170.666667c0 42.666667-51.2 64-81.066667 34.133333l-21.333333-29.866667c-17.066667-17.066667-46.933333-17.066667-68.266667 0L174.933333 243.2c-17.066667 17.066667-17.066667 46.933333 0 68.266667l25.6 25.6c29.866667 29.866667 8.533333 81.066667-29.866666 81.066666h-38.4c-25.6 0-46.933333 21.333333-46.933334 46.933334v93.866666c0 25.6 21.333333 46.933333 46.933334 46.933334H170.666667c42.666667 0 64 51.2 34.133333 81.066666l-25.6 25.6c-17.066667 17.066667-17.066667 46.933333 0 68.266667l68.266667 68.266667c17.066667 17.066667 46.933333 17.066667 68.266666 0l25.6-25.6c29.866667-29.866667 81.066667-8.533333 81.066667 34.133333v34.133333c0 25.6 21.333333 46.933333 46.933333 46.933334h93.866667c25.6 0 46.933333-21.333333 46.933333-46.933334V853.333333c0-42.666667 51.2-64 81.066667-34.133333l25.6 25.6c17.066667 17.066667 46.933333 17.066667 68.266667 0l68.266666-68.266667c17.066667-17.066667 17.066667-46.933333 0-68.266666l-25.6-25.6c-29.866667-29.866667-8.533333-81.066667 34.133334-81.066667h34.133333c25.6 0 46.933333-21.333333 46.933333-46.933333v-93.866667c-4.266667-21.333333-25.6-42.666667-51.2-42.666667zM512 695.466667c-102.4 0-183.466667-81.066667-183.466667-183.466667S409.6 328.533333 512 328.533333s183.466667 81.066667 183.466667 183.466667-81.066667 183.466667-183.466667 183.466667z"
              fill="#8C948B" p-id="21476"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.settings') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
      <div class="menu-item" @click="navigateTo('/pages/profile/about')">
        <div class="menu-icon">
          <svg t="1776004042078" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="23382" width="256" height="256">
            <path
              d="M512 51.2C257.6384 51.2 51.2 257.6384 51.2 512s206.4384 460.8 460.8 460.8 460.8-206.4384 460.8-460.8S766.3616 51.2 512 51.2z m-36.864 239.616c0-20.2752 16.5888-36.864 36.864-36.864s36.864 16.5888 36.864 36.864v294.912c0 20.2752-16.5888 36.864-36.864 36.864s-36.864-16.5888-36.864-36.864V290.816zM512 770.048c-30.4128 0-55.296-24.8832-55.296-55.296s24.8832-55.296 55.296-55.296 55.296 24.8832 55.296 55.296-24.8832 55.296-55.296 55.296z"
              fill="#1E5B48" p-id="23383"></path>
          </svg>
        </div>
        <div class="menu-text">{{ t('profile.about') }}</div>
        <uni-icons class="menu-arrow" type="right" size="16" color="#8C948B" />
      </div>
    </div>

    <!-- 退出登录按钮 -->
    <button v-if="userInfo" class="logout-btn" @click="logout">{{ t('profile.logout') }}</button>
    </div>

    <TabBar current="profile" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo } from '@/api/user'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import TabBar from '@/components/tabbar/index.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.profile')

// 用户信息
const userInfo = ref(null)
const loading = ref(false)
const error = ref(null)

// 导航到指定页面
const navigateTo = (url) => {
  uni.navigateTo({
    url
  })
}

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await getUserInfo()
    if (response.code === 200) {
      userInfo.value = response.data
      // 存储用户信息到本地
      uni.setStorageSync('userInfo', response.data)
    } else {
      error.value = response.message || '获取用户信息失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error('获取用户信息失败:', err)
  } finally {
    loading.value = false
  }
}

// 退出登录
const logout = () => {
  uni.showModal({
    title: t('profile.logoutTitle'),
    content: t('profile.logoutContent'),
    success: function (res) {
      if (res.confirm) {
        // 清除登录状态
        uni.removeStorageSync('userInfo')
        uni.removeStorageSync('token')
        // 跳转到登录页
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }
    }
  })
}

// 页面加载时获取用户信息
onMounted(() => {
  // 先尝试从本地存储获取用户信息
  const storedUserInfo = uni.getStorageSync('userInfo')
  if (storedUserInfo) {
    userInfo.value = storedUserInfo
  }
  // 无论是否有本地存储，都重新从API获取最新信息
  fetchUserInfo()
})
</script>

<style scoped>
/* 用户信息 Hero */
.profile-hero {
  position: relative;
  overflow: hidden;
  padding: 24px 20px;
  border-radius: var(--radius-xl);
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  color: #fff;
}

.hero-glow {
  position: absolute;
  top: -70px;
  right: -40px;
  width: 210px;
  height: 210px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(40px);
  pointer-events: none;
}

.hero-ring {
  position: absolute;
  right: -46px;
  bottom: -84px;
  width: 170px;
  height: 170px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.18);
  pointer-events: none;
}

.profile-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  flex: 0 0 auto;
  width: 74px;
  height: 74px;
  padding: 3px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 20px rgba(6, 24, 60, 0.18);
}

.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.7);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name-row h2 {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.3px;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-tag {
  flex: 0 0 auto;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.32);
  font-size: 10px;
  font-weight: 700;
}

.user-sign {
  margin-top: 5px;
  font-size: 12px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.82);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.login-cta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.34);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  transition: all 0.28s var(--ease-out);
}

.login-cta:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-1px);
}

/* 菜单面板 */
.panel {
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.25s var(--ease-out), padding-left 0.25s var(--ease-out);
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: var(--surface-2);
  padding-left: 20px;
}

.menu-icon {
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--surface-2);
  transition: transform 0.3s var(--ease-spring);
}

.menu-item:hover .menu-icon {
  transform: scale(1.08);
}

.menu-icon svg {
  width: 22px;
  height: 22px;
}

.menu-text {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-color);
}

.menu-arrow {
  display: flex;
  align-items: center;
  color: var(--text-tertiary-color);
  transition: transform 0.25s var(--ease-out);
}

.menu-item:hover .menu-arrow {
  transform: translateX(3px);
}

/* 退出登录 */
.logout-btn {
  width: 100%;
  height: 48px;
  margin-top: 24px;
  border-radius: var(--radius-pill);
  background: var(--card-background);
  border: 1px solid rgba(240, 68, 56, 0.28);
  color: #F04438;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.28s var(--ease-out);
}

.logout-btn:hover {
  background: rgba(240, 68, 56, 0.08);
  border-color: rgba(240, 68, 56, 0.45);
}

.logout-btn:active {
  transform: scale(0.985);
}

@media (min-width: 768px) {
  .profile-hero {
    padding: 34px 38px;
  }

  .avatar {
    width: 92px;
    height: 92px;
  }

  .user-name-row h2 {
    font-size: 25px;
  }

  .user-sign {
    font-size: 14px;
  }

  .menu-item {
    padding: 16px 22px;
  }

  .menu-text {
    font-size: 16px;
  }
}
</style>