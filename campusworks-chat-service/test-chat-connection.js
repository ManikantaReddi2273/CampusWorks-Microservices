const axios = require('axios');

async function testChatService() {
  const baseUrl = 'http://localhost:3001';
  
  console.log('🧪 Testing Chat Service Connection...');
  
  try {
    // Test health endpoint
    console.log('1. Testing health endpoint...');
    const healthResponse = await axios.get(`${baseUrl}/health`);
    console.log('✅ Health check passed:', healthResponse.data);
    
    // Test API info endpoint
    console.log('2. Testing API info endpoint...');
    const infoResponse = await axios.get(`${baseUrl}/api/info`);
    console.log('✅ API info retrieved:', infoResponse.data);
    
    // Test task endpoint (if Spring Boot is running)
    console.log('3. Testing task service integration...');
    try {
      const taskResponse = await axios.get(`${baseUrl}/test-task/1`);
      console.log('✅ Task service integration working:', taskResponse.data);
    } catch (taskError) {
      console.log('⚠️ Task service integration failed (expected if Spring Boot not running):', taskError.message);
    }
    
    console.log('🎉 Chat service is running and accessible!');
    
  } catch (error) {
    console.error('❌ Chat service test failed:', error.message);
    if (error.code === 'ECONNREFUSED') {
      console.error('💡 Make sure the chat service is running on port 3001');
    }
  }
}

testChatService();
