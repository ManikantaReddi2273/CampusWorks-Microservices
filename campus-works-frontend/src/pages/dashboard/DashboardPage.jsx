import React from 'react';
import { useSelector } from 'react-redux';
import { Container, Typography, Paper, Grid, Button, Box, Chip } from '@mui/material';
import { Add, Assignment, Gavel, AccountBalanceWallet, TrendingUp } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import Layout from '@components/templates/Layout';
import { selectAuth } from '@store/slices/authSlice';
import { ROUTES } from '@constants';

const DashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useSelector(selectAuth);

  const quickActions = [
    {
      title: 'Create New Task',
      description: 'Post a new academic task for others to bid on',
      icon: <Add />,
      action: () => navigate(ROUTES.CREATE_TASK),
      color: 'primary'
    },
    {
      title: 'Browse Tasks',
      description: 'Find tasks to bid on and earn money',
      icon: <Assignment />,
      action: () => navigate(ROUTES.TASKS),
      color: 'secondary'
    },
    {
      title: 'My Bids',
      description: 'Check the status of your bids',
      icon: <Gavel />,
      action: () => navigate(ROUTES.BIDS),
      color: 'info'
    },
    {
      title: 'Wallet',
      description: 'View your earnings and transactions',
      icon: <AccountBalanceWallet />,
      action: () => navigate(ROUTES.PAYMENTS),
      color: 'success'
    }
  ];

  return (
    <Layout>
      <Container maxWidth="lg">
        {/* Welcome Section */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Welcome back, {user?.email?.split('@')[0] || 'Student'}! 👋
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Your peer-to-peer academic task platform dashboard
          </Typography>
          <Chip 
            label={`Role: ${user?.role || 'STUDENT'}`} 
            color="primary" 
            variant="outlined" 
            icon={<TrendingUp />}
          />
        </Box>

        {/* Quick Actions */}
        <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          Quick Actions
        </Typography>
        
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {quickActions.map((action, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Paper 
                sx={{ 
                  p: 3, 
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4
                  }
                }}
                onClick={action.action}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <Box 
                    sx={{ 
                      p: 1, 
                      borderRadius: 1, 
                      bgcolor: `${action.color}.light`,
                      color: `${action.color}.contrastText`,
                      mr: 2
                    }}
                  >
                    {action.icon}
                  </Box>
                  <Typography variant="h6" component="h3">
                    {action.title}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ flexGrow: 1 }}>
                  {action.description}
                </Typography>
                <Button 
                  variant="outlined" 
                  color={action.color}
                  size="small"
                  sx={{ mt: 2, alignSelf: 'flex-start' }}
                  onClick={(e) => {
                    e.stopPropagation();
                    action.action();
                  }}
                >
                  Go
                </Button>
              </Paper>
            </Grid>
          ))}
        </Grid>

        {/* Stats Overview */}
        <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 3 }}>
          Overview
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom color="primary">
                🎯 Getting Started
              </Typography>
              <Typography variant="body1" paragraph>
                Welcome to CampusWorks! Here you can:
              </Typography>
              <Box component="ul" sx={{ pl: 2 }}>
                <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                  Post academic tasks and get help from fellow students
                </Typography>
                <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                  Bid on tasks to earn money using your skills
                </Typography>
                <Typography component="li" variant="body2" sx={{ mb: 1 }}>
                  Build your reputation through quality work
                </Typography>
                <Typography component="li" variant="body2">
                  Manage payments securely through our escrow system
                </Typography>
              </Box>
            </Paper>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom color="secondary">
                📊 Quick Stats
              </Typography>
              <Typography variant="body1" paragraph>
                Your activity summary:
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2">Tasks Created:</Typography>
                  <Chip label="0" size="small" />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2">Bids Placed:</Typography>
                  <Chip label="0" size="small" />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2">Tasks Completed:</Typography>
                  <Chip label="0" size="small" />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography variant="body2">Total Earnings:</Typography>
                  <Chip label="₹0.00" size="small" color="success" />
                </Box>
              </Box>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                * Stats will update as you use the platform
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Layout>
  );
};

export default DashboardPage;
