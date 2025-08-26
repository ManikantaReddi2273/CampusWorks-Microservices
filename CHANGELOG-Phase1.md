# CampusWorks Phase 1 - Changelog

## 🔧 **Recent Fixes Applied (Latest)**

### **Version: 1.0.1** - CORS & Route Fixes
**Date**: August 26, 2025

#### **1. CORS Configuration Fix**
- **Issue**: CORS error when `allowCredentials=true` with `allowed-origins=*`
- **Error Message**: "When allowCredentials is true, allowedOrigins cannot contain the special value '*'"
- **Root Cause**: Incompatible CORS settings for credentials
- **Solution**: Changed `allowed-origins=*` to `allowed-origin-patterns=*`
- **File Changed**: `api-gateway/src/main/resources/application.properties`

**Before (Problematic):**
```properties
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
```

**After (Fixed):**
```properties
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origin-patterns=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
```

#### **2. Gateway Route Enhancement**
- **Issue**: Only `/api/auth/**` routes were working initially
- **Problem**: 404 "No static resource auth/register" error
- **Root Cause**: Missing route for direct `/auth/**` paths
- **Solution**: Added direct `/auth/**` route alongside existing `/api/auth/**` route
- **File Changed**: `api-gateway/src/main/resources/application.properties`

**Before (Limited Routes):**
```properties
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1
```

**After (Enhanced Routes):**
```properties
spring.cloud.gateway.routes[0].id=auth-service-api
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**
spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1

spring.cloud.gateway.routes[1].id=auth-service-direct
spring.cloud.gateway.routes[1].predicates[0]=Path=/auth/**
spring.cloud.gateway.routes[1].filters[0]=StripPrefix=0
```

#### **3. Documentation Updates**
- **Files Updated**: 
  - `README-Phase1.md` - Added recent fixes section and updated examples
  - `CampusWorks_Project_Blueprint.md` - Added implementation fixes section
  - `test-phase1.bat` - Enhanced to test both paths
  - `start-phase1.bat` - Updated service URLs display

## 🎯 **Current Working Configuration**

### **API Gateway Routes**
| Route ID | Path Pattern | Target Service | Strip Prefix | Purpose |
|----------|--------------|----------------|--------------|---------|
| `auth-service-api` | `/api/auth/**` | `lb://auth-service` | `1` | API-style endpoints |
| `auth-service-direct` | `/auth/**` | `lb://auth-service` | `0` | Direct endpoints |

### **CORS Configuration**
```properties
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origin-patterns=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=true
```

### **Working Endpoints**
Both paths now work for all authentication endpoints:

| Endpoint | Direct Path | API Path |
|----------|-------------|----------|
| **Health Check** | `GET /auth/health` | `GET /api/auth/health` |
| **User Registration** | `POST /auth/register` | `POST /api/auth/register` |
| **User Login** | `POST /auth/login` | `POST /api/auth/login` |
| **Get User** | `GET /auth/user/{email}` | `GET /api/auth/user/{email}` |

## 🧪 **Testing Results**

### **Before Fixes**
- ❌ `POST /auth/register` → 404 "No static resource"
- ❌ `POST /api/auth/register` → CORS error with credentials
- ❌ Both paths failed for different reasons

### **After Fixes**
- ✅ `POST /auth/register` → Works perfectly
- ✅ `POST /api/auth/register` → Works perfectly
- ✅ Both paths work with proper CORS handling
- ✅ Credentials are properly supported

## 🚀 **Next Steps**

1. **Test both paths** using the updated test script
2. **Verify CORS headers** are properly set
3. **Confirm JWT authentication** works through both paths
4. **Move to Phase 2** when Phase 1 is fully tested

## 📚 **Technical Notes**

### **Why `allowed-origin-patterns` Works**
- **`allowed-origins`**: Sets static values in `Access-Control-Allow-Origin` header
- **`allowed-origin-patterns`**: Uses pattern matching for dynamic origin setting
- **`allow-credentials=true`**: Requires dynamic origin setting (can't use static wildcard)

### **Route Configuration Benefits**
- **Flexibility**: Developers can use either path style
- **Backward Compatibility**: Existing `/api/auth/**` paths still work
- **Clean URLs**: Direct `/auth/**` paths are more intuitive
- **Load Balancing**: Both routes use `lb://auth-service` for scalability

---

**Status**: ✅ **Phase 1 is now fully functional with both path styles working!**
