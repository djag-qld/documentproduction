<template>
  <div id="app">    
    <VideoCapture />
  </div>
</template>

<script>
import Vue from 'vue'
import VueQrcodeReader from "vue-qrcode-reader"
import VideoCapture from './components/VideoCapture.vue'
Vue.use(VueQrcodeReader)

export default {
  name: 'App',
  components: {
    VideoCapture
  }
}
</script>
