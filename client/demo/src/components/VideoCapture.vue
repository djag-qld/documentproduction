<template>
  <div>
    <p class="error" v-if="cameraError != ''">Camera status: {{ cameraError }}</p>
    
    <div v-if="cameraError == ''">
      <div class="stream">
        <qrcode-stream @decode="onDecode" @init="onInit" />
      </div>
    </div>
    <div class="fileupload">
      <qrcode-capture @decode="onDecode"></qrcode-capture>
    </div>
    <p v-if="result != ''">
      Verfied: <b v-if="verifyResult">Verified</b><b v-else>Not verified</b><br/>
      Created: <b>{{ result.cdate }}</b><br/>
      Document ID: <b>{{ result.dId }}</b><br/>
      Fields: <b>{{ result.f }}</b><br/>
    </p>
    
    <p>Certificate</p>
    <textarea @change="onDecode" class="certificate" v-model="certificate"></textarea>    
  </div>
</template>

<script>
const base45 = require('base45')
const Buffer = require('buffer').Buffer
const zlib = require('zlib')
const crypto = require("crypto")
import cert from '../assets/testcert.txt'

export default {
  name: 'QRScanner',
  data() {
    return {
      result: '',
      error: '',
      cameraError: '',
      verifyResult: false,
      certificate: cert
    }
  },
  methods: {
    async onInit(promise) {
      try {
        await promise
      } catch(error) {
        this.cameraError = error.name
      }
    },
    onDecode(decodedString) {
      console.log('Checking image signature')
      try {
        const compressed = base45.decode(decodedString)
        const input = Buffer.from(compressed)
        zlib.inflate(input, (e, r) => {
          this.result = JSON.parse(r)
          const signature = base45.decode(this.result.sig)
          const verifier = crypto.createVerify('rsa-sha256')
          verifier.update(JSON.stringify(this.result.f))          
          this.verifyResult =  verifier.verify(this.certificate, signature, 'binary')
        })
      } catch(error) {
        console.error(error)
        this.error = error.name        
      }
    }
  }
}
</script>
<style>
.stream {
  width: 100%;
  height: 20em;
}
.certificate {
  width: 41em;
  height: 30em;
}
</style>