import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Button,
  Divider,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  Card,
  CardContent,
  Grid,
  Chip,
  IconButton,
  Tooltip
} from '@mui/material';
import {
  Delete,
  Edit,
  Security,
  Email,
  Person,
  Warning,
  Logout,
  Save,
  Cancel,
  Visibility,
  VisibilityOff
} from '@mui/icons-material';
import Layout from '@components/templates/Layout';
import { selectAuth, logoutUser } from '@store/slices/authSlice';
import { ROUTES } from '@constants';
import apiService from '@services/api';

const ProfilePage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user, loading } = useSelector(selectAuth);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [confirmEmail, setConfirmEmail] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  // Change password state
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);
  const [changePasswordData, setChangePasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [changePasswordErrors, setChangePasswordErrors] = useState({});
  const [changePasswordTouched, setChangePasswordTouched] = useState({});
  const [changePasswordLoading, setChangePasswordLoading] = useState(false);
  const [changePasswordError, setChangePasswordError] = useState('');
  const [changePasswordSuccess, setChangePasswordSuccess] = useState('');
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false
  });

  // Resend verification state
  const [resendVerificationLoading, setResendVerificationLoading] = useState(false);
  const [resendVerificationError, setResendVerificationError] = useState('');
  const [resendVerificationSuccess, setResendVerificationSuccess] = useState('');

  const handleDeleteAccount = async () => {
    if (confirmEmail !== user?.email) {
      setDeleteError('Email does not match your account email');
      return;
    }

    setDeleteLoading(true);
    setDeleteError('');

    try {
      const response = await apiService.auth.deleteAccount(user.email);
      
      if (response.data.success) {
        // Logout user after successful deletion
        dispatch(logoutUser());
        navigate(ROUTES.LOGIN);
      } else {
        setDeleteError(response.data.message || 'Failed to delete account. Please try again.');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to delete account. Please try again.';
      setDeleteError(errorMessage);
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleLogout = () => {
    dispatch(logoutUser());
    navigate(ROUTES.LOGIN);
  };

  // Password validation functions
  const validateNewPassword = (password) => {
    if (!password) {
      return 'New password is required';
    }
    
    if (password.length < 8) {
      return 'Password must be at least 8 characters long';
    }
    
    if (!/(?=.*[a-z])/.test(password)) {
      return 'Password must contain at least one lowercase letter';
    }
    
    if (!/(?=.*[A-Z])/.test(password)) {
      return 'Password must contain at least one uppercase letter';
    }
    
    if (!/(?=.*\d)/.test(password)) {
      return 'Password must contain at least one number';
    }
    
    if (!/(?=.*[@$!%*?&])/.test(password)) {
      return 'Password must contain at least one special character (@$!%*?&)';
    }
    
    return '';
  };

  const validateConfirmPassword = (confirmPassword) => {
    if (!confirmPassword) {
      return 'Please confirm your new password';
    }
    
    if (changePasswordData.newPassword !== confirmPassword) {
      return 'Passwords do not match';
    }
    
    return '';
  };

  const handleChangePasswordChange = (e) => {
    const { name, value } = e.target;
    
    setChangePasswordData({
      ...changePasswordData,
      [name]: value
    });
    
    // Clear field-specific errors
    if (changePasswordErrors[name]) {
      setChangePasswordErrors({
        ...changePasswordErrors,
        [name]: ''
      });
    }
  };

  const handleChangePasswordBlur = (e) => {
    const { name } = e.target;
    setChangePasswordTouched({
      ...changePasswordTouched,
      [name]: true
    });
  };

  const validateChangePasswordForm = () => {
    const errors = {};
    
    if (!changePasswordData.currentPassword) {
      errors.currentPassword = 'Current password is required';
    }
    
    errors.newPassword = validateNewPassword(changePasswordData.newPassword);
    errors.confirmPassword = validateConfirmPassword(changePasswordData.confirmPassword);
    
    setChangePasswordErrors(errors);
    return Object.keys(errors).filter(key => errors[key]).length === 0;
  };

  const handleChangePasswordSubmit = async () => {
    // Mark all fields as touched to show validation errors
    setChangePasswordTouched({
      currentPassword: true,
      newPassword: true,
      confirmPassword: true
    });
    
    if (!validateChangePasswordForm()) {
      return;
    }
    
    setChangePasswordLoading(true);
    setChangePasswordError('');
    setChangePasswordSuccess('');

    try {
      const response = await apiService.auth.changePassword({
        currentPassword: changePasswordData.currentPassword,
        newPassword: changePasswordData.newPassword
      });
      
      if (response.data.success) {
        setChangePasswordSuccess('Password changed successfully!');
        // Clear form
        setChangePasswordData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
        // Close dialog after 2 seconds
        setTimeout(() => {
          setChangePasswordOpen(false);
          setChangePasswordSuccess('');
        }, 2000);
      } else {
        setChangePasswordError(response.data.message || 'Failed to change password');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to change password. Please try again.';
      setChangePasswordError(errorMessage);
    } finally {
      setChangePasswordLoading(false);
    }
  };

  const handleCloseChangePassword = () => {
    setChangePasswordOpen(false);
    setChangePasswordData({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    setChangePasswordErrors({});
    setChangePasswordTouched({});
    setChangePasswordError('');
    setChangePasswordSuccess('');
    setShowPasswords({
      current: false,
      new: false,
      confirm: false
    });
  };

  const handleResendVerification = async () => {
    if (!user?.email) {
      setResendVerificationError('User email not found');
      return;
    }

    setResendVerificationLoading(true);
    setResendVerificationError('');
    setResendVerificationSuccess('');

    try {
      const response = await apiService.auth.resendVerification();
      
      if (response.data.success) {
        setResendVerificationSuccess('Verification email sent successfully! Please check your inbox.');
        // Clear success message after 5 seconds
        setTimeout(() => {
          setResendVerificationSuccess('');
        }, 5000);
      } else {
        setResendVerificationError(response.data.message || 'Failed to resend verification email');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to resend verification email. Please try again.';
      setResendVerificationError(errorMessage);
    } finally {
      setResendVerificationLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Not available';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <Layout>
      <Container maxWidth="md">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Profile & Settings
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your account settings and preferences
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {/* Profile Information */}
          <Grid item xs={12} md={8}>
            <Paper sx={{ p: 3, mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Person sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6">Account Information</Typography>
              </Box>
              
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Email Address
                  </Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {user?.email || 'Not available'}
                  </Typography>
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Account Role
                  </Typography>
                  <Chip 
                    label={user?.role || 'STUDENT'} 
                    color="primary" 
                    variant="outlined"
                    sx={{ mb: 2 }}
                  />
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Email Verified
                  </Typography>
                  <Chip 
                    label={user?.emailVerified ? 'Verified' : 'Not Verified'} 
                    color={user?.emailVerified ? 'success' : 'warning'} 
                    variant="outlined"
                    sx={{ mb: 2 }}
                  />
                </Grid>
                
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Account Created
                  </Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {formatDate(user?.createdAt)}
                  </Typography>
                </Grid>
              </Grid>
            </Paper>

            {/* Security Settings */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Security sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6">Security Settings</Typography>
              </Box>
              
              <Button
                variant="outlined"
                color="primary"
                startIcon={<Edit />}
                onClick={() => setChangePasswordOpen(true)}
                sx={{ mr: 2, mb: 2 }}
              >
                Change Password
              </Button>
              
              <Button
                variant="outlined"
                color="secondary"
                startIcon={<Email />}
                onClick={handleResendVerification}
                disabled={resendVerificationLoading}
                sx={{ mb: 2 }}
              >
                {resendVerificationLoading ? 'Sending...' : 'Resend Verification Email'}
              </Button>
              
              {resendVerificationError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {resendVerificationError}
                </Alert>
              )}
              
              {resendVerificationSuccess && (
                <Alert severity="success" sx={{ mb: 2 }}>
                  {resendVerificationSuccess}
                </Alert>
              )}
            </Paper>
          </Grid>

          {/* Account Actions */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom>
                Account Actions
              </Typography>
              
              <Button
                variant="outlined"
                color="info"
                fullWidth
                startIcon={<Logout />}
                onClick={handleLogout}
                sx={{ mb: 2 }}
              >
                Sign Out
              </Button>
              
              <Divider sx={{ my: 2 }} />
              
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Danger Zone
              </Typography>
              
              <Button
                variant="outlined"
                color="error"
                fullWidth
                startIcon={<Delete />}
                onClick={() => setDeleteDialogOpen(true)}
              >
                Delete Account
              </Button>
              
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                This action cannot be undone. All your data will be permanently deleted.
              </Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Delete Account Dialog */}
        <Dialog 
          open={deleteDialogOpen} 
          onClose={() => setDeleteDialogOpen(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle sx={{ color: 'error.main' }}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Warning sx={{ mr: 1, color: 'error.main' }} />
              Delete Account
            </Box>
          </DialogTitle>
          
          <DialogContent>
            <Alert severity="warning" sx={{ mb: 2 }}>
              <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                This action is irreversible!
              </Typography>
              <Typography variant="body2">
                Deleting your account will permanently remove all your data including:
              </Typography>
              <Box component="ul" sx={{ mt: 1, pl: 2 }}>
                <Typography component="li" variant="body2">
                  All created tasks and bids
                </Typography>
                <Typography component="li" variant="body2">
                  Account balance and transaction history
                </Typography>
                <Typography component="li" variant="body2">
                  Profile information and ratings
                </Typography>
                <Typography component="li" variant="body2">
                  All associated data and files
                </Typography>
              </Box>
            </Alert>
            
            <Typography variant="body1" sx={{ mb: 2 }}>
              To confirm deletion, please enter your email address:
            </Typography>
            
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              <strong>{user?.email}</strong>
            </Typography>
            
            <TextField
              fullWidth
              label="Confirm Email"
              value={confirmEmail}
              onChange={(e) => setConfirmEmail(e.target.value)}
              error={!!deleteError}
              helperText={deleteError}
              placeholder="Enter your email to confirm"
              sx={{ mt: 1 }}
            />
          </DialogContent>
          
          <DialogActions sx={{ p: 3 }}>
            <Button 
              onClick={() => setDeleteDialogOpen(false)}
              disabled={deleteLoading}
            >
              Cancel
            </Button>
            <Button
              variant="contained"
              color="error"
              onClick={handleDeleteAccount}
              disabled={deleteLoading || confirmEmail !== user?.email}
              startIcon={deleteLoading ? <CircularProgress size={20} /> : <Delete />}
            >
              {deleteLoading ? 'Deleting...' : 'Delete Account'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Change Password Dialog */}
        <Dialog 
          open={changePasswordOpen} 
          onClose={handleCloseChangePassword}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            Change Password
          </DialogTitle>
          
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Enter your current password and choose a new password.
            </Typography>
            
            {changePasswordError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {changePasswordError}
              </Alert>
            )}
            
            {changePasswordSuccess && (
              <Alert severity="success" sx={{ mb: 2 }}>
                {changePasswordSuccess}
              </Alert>
            )}
            
            <TextField
              fullWidth
              label="Current Password"
              type={showPasswords.current ? 'text' : 'password'}
              name="currentPassword"
              value={changePasswordData.currentPassword}
              onChange={handleChangePasswordChange}
              onBlur={handleChangePasswordBlur}
              error={changePasswordTouched.currentPassword && !!changePasswordErrors.currentPassword}
              helperText={changePasswordTouched.currentPassword && changePasswordErrors.currentPassword}
              disabled={changePasswordLoading}
              sx={{ mb: 2 }}
              InputProps={{
                endAdornment: (
                  <Button
                    onClick={() => setShowPasswords({...showPasswords, current: !showPasswords.current})}
                    sx={{ minWidth: 'auto', p: 1 }}
                  >
                    {showPasswords.current ? <VisibilityOff /> : <Visibility />}
                  </Button>
                )
              }}
            />
            
            <TextField
              fullWidth
              label="New Password"
              type={showPasswords.new ? 'text' : 'password'}
              name="newPassword"
              value={changePasswordData.newPassword}
              onChange={handleChangePasswordChange}
              onBlur={handleChangePasswordBlur}
              error={changePasswordTouched.newPassword && !!changePasswordErrors.newPassword}
              helperText={changePasswordTouched.newPassword && changePasswordErrors.newPassword ? changePasswordErrors.newPassword : "Must be at least 8 characters with uppercase, lowercase, number, and special character"}
              disabled={changePasswordLoading}
              sx={{ mb: 2 }}
              InputProps={{
                endAdornment: (
                  <Button
                    onClick={() => setShowPasswords({...showPasswords, new: !showPasswords.new})}
                    sx={{ minWidth: 'auto', p: 1 }}
                  >
                    {showPasswords.new ? <VisibilityOff /> : <Visibility />}
                  </Button>
                )
              }}
            />
            
            <TextField
              fullWidth
              label="Confirm New Password"
              type={showPasswords.confirm ? 'text' : 'password'}
              name="confirmPassword"
              value={changePasswordData.confirmPassword}
              onChange={handleChangePasswordChange}
              onBlur={handleChangePasswordBlur}
              error={changePasswordTouched.confirmPassword && !!changePasswordErrors.confirmPassword}
              helperText={changePasswordTouched.confirmPassword && changePasswordErrors.confirmPassword ? changePasswordErrors.confirmPassword : "Re-enter your new password"}
              disabled={changePasswordLoading}
              InputProps={{
                endAdornment: (
                  <Button
                    onClick={() => setShowPasswords({...showPasswords, confirm: !showPasswords.confirm})}
                    sx={{ minWidth: 'auto', p: 1 }}
                  >
                    {showPasswords.confirm ? <VisibilityOff /> : <Visibility />}
                  </Button>
                )
              }}
            />
          </DialogContent>
          
          <DialogActions sx={{ p: 3 }}>
            <Button 
              onClick={handleCloseChangePassword}
              disabled={changePasswordLoading}
            >
              Cancel
            </Button>
            <Button
              variant="contained"
              onClick={handleChangePasswordSubmit}
              disabled={changePasswordLoading}
              startIcon={changePasswordLoading ? <CircularProgress size={20} /> : null}
            >
              {changePasswordLoading ? 'Changing...' : 'Change Password'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </Layout>
  );
};

export default ProfilePage;
