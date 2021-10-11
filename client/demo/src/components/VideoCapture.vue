<template>
  <div>
    <p class="error" v-if="cameraError != '' && cameraError != 'NotFoundError'">Camera status: {{ cameraError }}</p>
    
    <div v-if="cameraError == '' && result == ''">
      <div class="stream">
        <qrcode-stream @decode="onDecode" @init="onInit" />
      </div>
    </div>
    <div class="fileupload" v-if="result == ''">
      <qrcode-capture @decode="onDecode"></qrcode-capture>
    </div>
    <p v-if="processing">Processing...</p>
    <p v-if="result != ''">
      Verification: <b class="verified" v-if="verifyResult">Verified</b><b class="notverified" v-else>Not verified</b><br/>
      Created: <b>{{ result.cdate }}</b><br/>
      Document ID: <b>{{ result.dId }}</b><br/>
      <ul>
        <li v-for="(f, fname) in result.f" v-bind:key="f">
          {{ fname }}: <b>{{ f }}</b>
        </li>
      </ul>
    </p>
    <button v-if="result != ''" @click="reset">Reset</button>
    <hr/>
    <p>Verification certificate:</p>
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
      certificate: cert,
      processing: false
    }
  },
  created() {
    document.title = 'Demonstration client for QR verficiation'
  },
  methods: {
    async onInit(promise) {
      try {
        await promise
      } catch(error) {
        this.cameraError = error.name
      }
    },
    reset() {
      this.result = ''
    },
    onDecode(decodedString) {
      console.log('Checking image signature')
      this.processing = true
      try {
        const compressed = base45.decode(decodedString)
        const input = Buffer.from(compressed)
        zlib.inflate(input, (e, r) => {
          this.result = JSON.parse(r)
          const signature = base45.decode(this.result.sig)
          const verifier = crypto.createVerify('rsa-sha256')
          delete this.result.sig
          verifier.update(JSON.stringify(this.result))
          this.verifyResult =  verifier.verify(this.certificate, signature, 'binary')
        })
      } catch(error) {
        console.error(error)
        this.error = error.name        
      }
      this.processing = false
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
.verified {
  color: green;
}
.notverified {
  color: red;
}
</style>