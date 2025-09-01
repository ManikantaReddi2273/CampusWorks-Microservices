import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import { loginUser, selectAuthLoading, selectAuthError } from '@store/slices/authSlice';
import { ROUTES } from '@constants';
import apiService from '@services/api';

const LoginPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  
  const isLoading = useSelector(selectAuthLoading);
  const error = useSelector(selectAuthError);
  
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });

  // Forgot password state
  const [forgotPasswordOpen, setForgotPasswordOpen] = useState(false);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState('');
  const [forgotPasswordLoading, setForgotPasswordLoading] = useState(false);
  const [forgotPasswordError, setForgotPasswordError] = useState('');
  const [forgotPasswordSuccess, setForgotPasswordSuccess] = useState('');

  const from = location.state?.from?.pathname || ROUTES.DASHBOARD;

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const result = await dispatch(loginUser(formData));
    
    if (loginUser.fulfilled.match(result)) {
      navigate(from, { replace: true });
    }
  };

  const handleForgotPassword = async () => {
    if (!forgotPasswordEmail.trim()) {
      setForgotPasswordError('Please enter your email address');
      return;
    }

    setForgotPasswordLoading(true);
    setForgotPasswordError('');
    setForgotPasswordSuccess('');

    try {
      const response = await apiService.auth.forgotPassword(forgotPasswordEmail);
      
      if (response.data.success) {
        setForgotPasswordSuccess('Password reset email sent successfully! Check your inbox.');
        setForgotPasswordEmail('');
      } else {
        setForgotPasswordError(response.data.message || 'Failed to send reset email');
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'Failed to send reset email. Please try again.';
      setForgotPasswordError(errorMessage);
    } finally {
      setForgotPasswordLoading(false);
    }
  };

  const handleCloseForgotPassword = () => {
    setForgotPasswordOpen(false);
    setForgotPasswordEmail('');
    setForgotPasswordError('');
    setForgotPasswordSuccess('');
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ padding: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            Welcome to CampusWorks
          </Typography>
          <Typography variant="body1" align="center" color="text.secondary" sx={{ mb: 3 }}>
            Sign in to your account
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email Address"
              name="email"
              autoComplete="email"
              autoFocus
              value={formData.email}
              onChange={handleChange}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={formData.password}
              onChange={handleChange}
            />
            
            {/* Forgot Password Link */}
            <Box textAlign="right" sx={{ mt: 1 }}>
              <Button
                variant="text"
                size="small"
                onClick={() => setForgotPasswordOpen(true)}
                sx={{ textTransform: 'none' }}
              >
                Forgot Password?
              </Button>
            </Box>
            
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={isLoading}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Sign In'}
            </Button>
            
            <Box textAlign="center">
              <Link to={ROUTES.REGISTER}>
                Don't have an account? Sign Up
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>

      {/* Forgot Password Dialog */}
      <Dialog 
        open={forgotPasswordOpen} 
        onClose={handleCloseForgotPassword}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          Reset Your Password
        </DialogTitle>
        
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Enter your email address and we'll send you a link to reset your password.
          </Typography>
          
          {forgotPasswordError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {forgotPasswordError}
            </Alert>
          )}
          
          {forgotPasswordSuccess && (
            <Alert severity="success" sx={{ mb: 2 }}>
              {forgotPasswordSuccess}
            </Alert>
          )}
          
          <TextField
            fullWidth
            label="Email Address"
            type="email"
            value={forgotPasswordEmail}
            onChange={(e) => setForgotPasswordEmail(e.target.value)}
            placeholder="Enter your email address"
            disabled={forgotPasswordLoading}
            sx={{ mt: 1 }}
          />
        </DialogContent>
        
        <DialogActions sx={{ p: 3 }}>
          <Button 
            onClick={handleCloseForgotPassword}
            disabled={forgotPasswordLoading}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleForgotPassword}
            disabled={forgotPasswordLoading || !forgotPasswordEmail.trim()}
            startIcon={forgotPasswordLoading ? <CircularProgress size={20} /> : null}
          >
            {forgotPasswordLoading ? 'Sending...' : 'Send Reset Link'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default LoginPage;
