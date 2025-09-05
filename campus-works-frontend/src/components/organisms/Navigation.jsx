import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Divider,
  CircularProgress
} from '@mui/material';
import {
  AccountCircle,
  Dashboard,
  Assignment,
  Gavel,
  Person,
  Payment,
  ExitToApp
} from '@mui/icons-material';
import { logoutUser, selectAuth, selectAuthLoading } from '@store/slices/authSlice';
import { ROUTES } from '@constants';

const Navigation = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  
  const { isAuthenticated, user } = useSelector(selectAuth);
  const isLoading = useSelector(selectAuthLoading);
  
  const [anchorEl, setAnchorEl] = React.useState(null);
  const open = Boolean(anchorEl);

  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleMenuClose();
    
    try {
      await dispatch(logoutUser()).unwrap();
      navigate(ROUTES.LOGIN);
    } catch (error) {
      // Even if logout fails, redirect to login for security
      navigate(ROUTES.LOGIN);
    }
  };

  const handleNavigation = (path) => {
    navigate(path);
    handleMenuClose();
  };

  // Don't show navigation on auth pages
  if (!isAuthenticated || location.pathname === ROUTES.LOGIN || location.pathname === ROUTES.REGISTER) {
    return null;
  }

  const navigationItems = [
    { label: 'Dashboard', path: ROUTES.DASHBOARD, icon: <Dashboard /> },
    { label: 'Tasks', path: ROUTES.TASKS, icon: <Assignment /> },
    { label: 'Bids', path: ROUTES.BIDS, icon: <Gavel /> },
    { label: 'Profile', path: ROUTES.PROFILE, icon: <Person /> },
    { label: 'Payments', path: ROUTES.PAYMENTS, icon: <Payment /> }
  ];

  return (
    <AppBar position="static" elevation={1} sx={{ backgroundColor: 'white', color: 'text.primary' }}>
      <Toolbar>
        {/* Logo/Brand */}
        <Typography
          variant="h6"
          component={Link}
          to={ROUTES.DASHBOARD}
          sx={{
            flexGrow: 0,
            mr: 4,
            textDecoration: 'none',
            color: 'inherit',
            fontWeight: 600
          }}
        >
          CampusWorks
        </Typography>

        {/* Navigation Links */}
        <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
          {navigationItems.map((item) => (
            <Button
              key={item.path}
              component={Link}
              to={item.path}
              color="inherit"
              startIcon={item.icon}
              sx={{
                mx: 1,
                color: 'text.primary',
                backgroundColor: location.pathname === item.path ? 'rgba(0, 0, 0, 0.04)' : 'transparent',
                '&:hover': {
                  backgroundColor: 'rgba(0, 0, 0, 0.04)'
                }
              }}
            >
              {item.label}
            </Button>
          ))}
        </Box>

        {/* User Menu */}
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Typography variant="body2" sx={{ mr: 2, display: { xs: 'none', sm: 'block' } }}>
            Welcome, {user?.email || 'User'}
          </Typography>
          
          <IconButton
            size="large"
            aria-label="account of current user"
            aria-controls={open ? 'account-menu' : undefined}
            aria-haspopup="true"
            aria-expanded={open ? 'true' : undefined}
            onClick={handleMenuClick}
            disabled={isLoading}
            sx={{
              color: '#000000',
              '&:hover': {
                color: '#1976d2',
                backgroundColor: 'rgba(25, 118, 210, 0.1)'
              }
            }}
          >
            {isLoading ? (
              <CircularProgress size={24} color="primary" />
            ) : (
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
                <AccountCircle />
              </Avatar>
            )}
          </IconButton>

          <Menu
            id="account-menu"
            anchorEl={anchorEl}
            open={open}
            onClose={handleMenuClose}
            onClick={handleMenuClose}
            PaperProps={{
              elevation: 3,
              sx: {
                overflow: 'visible',
                filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                mt: 1.5,
                minWidth: 200,
                '& .MuiAvatar-root': {
                  width: 32,
                  height: 32,
                  ml: -0.5,
                  mr: 1,
                },
                '&:before': {
                  content: '""',
                  display: 'block',
                  position: 'absolute',
                  top: 0,
                  right: 14,
                  width: 10,
                  height: 10,
                  bgcolor: 'background.paper',
                  transform: 'translateY(-50%) rotate(45deg)',
                  zIndex: 0,
                },
              },
            }}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            {/* User Info */}
            <MenuItem disabled>
              <Avatar sx={{ bgcolor: 'primary.main' }}>
                <AccountCircle />
              </Avatar>
              <Box>
                <Typography variant="body2" fontWeight={500}>
                  {user?.email || 'User'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {user?.role || 'STUDENT'}
                </Typography>
              </Box>
            </MenuItem>
            
            <Divider />

            {/* Mobile Navigation */}
            <Box sx={{ display: { xs: 'block', md: 'none' } }}>
              {navigationItems.map((item) => (
                <MenuItem
                  key={item.path}
                  onClick={() => handleNavigation(item.path)}
                  selected={location.pathname === item.path}
                >
                  {item.icon}
                  <Typography sx={{ ml: 1 }}>{item.label}</Typography>
                </MenuItem>
              ))}
              <Divider />
            </Box>

            {/* Profile Link */}
            <MenuItem onClick={() => handleNavigation(ROUTES.PROFILE)}>
              <Person />
              <Typography sx={{ ml: 1 }}>My Profile</Typography>
            </MenuItem>

            {/* Logout */}
            <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
              <ExitToApp />
              <Typography sx={{ ml: 1 }}>Logout</Typography>
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navigation;
