<template>
  <div>
    <p class="error">Camera/scanning status: {{ error }}</p>
    
    <div v-if="error == ''">      
      <div class="stream">
        <qrcode-stream @decode="onDecode" @init="onInit" />
      </div>
    </div>
    <div v-else class="fileupload">
      <qrcode-capture @decode="onDecode"></qrcode-capture>
    </div>
    <p v-if="result != ''">
      Created: <b>{{ result.cdate }}</b><br/>
      Document ID: <b>{{ result.dId }}</b><br/>
      Fields: <b>{{ result.f }}</b><br/>
      Verfied: <b v-if="verifyResult">Verfied</b><b v-else>Not verified</b>
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
      verifyResult: false,
      certificate: cert
    }
  },
  methods: {
    async onInit(promise) {
      try {
        await promise
      } catch(error) {
        this.error = error.name
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