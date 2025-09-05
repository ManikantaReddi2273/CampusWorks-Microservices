import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Card,
  CardContent,
  CardActions,
  Button,
  Chip,
  Grid,
  CircularProgress,
  Alert,
  IconButton,
  Tooltip,
  Divider,
  Stack
} from '@mui/material';
import {
  Visibility,
  Gavel,
  AttachMoney,
  Person,
  Schedule,
  CheckCircle,
  Cancel,
  Assignment
} from '@mui/icons-material';
import { selectAuth } from '@store/slices/authSlice';
import { ROUTES } from '@constants';
import apiService from '@services/api';
import CountdownTimer from '@components/common/CountdownTimer';

const BidsPage = () => {
  const navigate = useNavigate();
  const { user } = useSelector(selectAuth);
  
  const [tasks, setTasks] = useState([]);
  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user?.email) {
      fetchUserTasksAndBids();
    }
  }, [user?.email]);

  const fetchUserTasksAndBids = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Fetch tasks owned by the current user
      const tasksResponse = await apiService.tasks.getByOwnerEmail(user.email);
      const userTasks = tasksResponse.data || [];
      setTasks(userTasks);
      
      // Fetch all bids for these tasks
      if (userTasks.length > 0) {
        const allBids = [];
        for (const task of userTasks) {
          try {
            const bidsResponse = await apiService.bids.getByTask(task.id);
            const taskBids = (bidsResponse.data || []).map(bid => ({
              ...bid,
              taskTitle: task.title,
              taskBudget: task.budget,
              taskStatus: task.status,
              taskBiddingDeadline: task.biddingDeadline,
              taskCompletionDeadline: task.completionDeadline
            }));
            allBids.push(...taskBids);
          } catch (error) {
            console.error(`Error fetching bids for task ${task.id}:`, error);
          }
        }
        setBids(allBids);
      }
      
    } catch (error) {
      console.error('Error fetching user tasks and bids:', error);
      setError('Failed to load bids. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleViewTask = (taskId) => {
    navigate(`${ROUTES.TASK_DETAIL.replace(':id', taskId)}`);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'ACCEPTED': return 'success';
      case 'REJECTED': return 'error';
      case 'WITHDRAWN': return 'default';
      default: return 'default';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'ACCEPTED': return <CheckCircle />;
      case 'REJECTED': return <Cancel />;
      default: return <Gavel />;
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-IN', {
      timeZone: 'Asia/Kolkata',
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getTimeRemaining = (deadline) => {
    const now = new Date();
    const deadlineDate = new Date(deadline);
    const diff = deadlineDate.getTime() - now.getTime();
    
    if (diff <= 0) {
      return { expired: true, text: 'Expired' };
    }
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    
    if (days > 0) {
      return { expired: false, text: `${days}d ${hours}h` };
    } else if (hours > 0) {
      return { expired: false, text: `${hours}h ${minutes}m` };
    } else {
      return { expired: false, text: `${minutes}m` };
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Bids on My Tasks
        </Typography>
        <Typography variant="body1" color="text.secondary">
          View and manage bids placed on your tasks
        </Typography>
      </Box>

      {bids.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Gavel sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            No Bids Yet
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            You haven't received any bids on your tasks yet. Create more tasks to attract bidders!
          </Typography>
          <Button
            variant="contained"
            startIcon={<Assignment />}
            onClick={() => navigate(ROUTES.CREATE_TASK)}
          >
            Create New Task
          </Button>
        </Paper>
      ) : (
        <Grid container spacing={3}>
          {bids.map((bid) => {
            const timeRemaining = getTimeRemaining(bid.taskBiddingDeadline);
            
            return (
              <Grid item xs={12} md={6} lg={4} key={bid.id}>
                <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                      <Typography variant="h6" component="h2" sx={{ fontWeight: 600 }}>
                        {bid.taskTitle}
                      </Typography>
                      <Chip
                        icon={getStatusIcon(bid.status)}
                        label={bid.status}
                        color={getStatusColor(bid.status)}
                        size="small"
                      />
                    </Box>

                    <Stack spacing={2}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <AttachMoney color="primary" />
                        <Typography variant="h6" color="primary" sx={{ fontWeight: 600 }}>
                          {formatCurrency(bid.amount)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          / {formatCurrency(bid.taskBudget)}
                        </Typography>
                      </Box>

                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Person color="action" />
                        <Typography variant="body2" color="text.secondary">
                          {bid.bidderEmail}
                        </Typography>
                      </Box>

                      <Box>
                        <Typography variant="body2" color="text.secondary" gutterBottom>
                          Proposal:
                        </Typography>
                        <Typography variant="body2" sx={{ 
                          display: '-webkit-box',
                          WebkitLineClamp: 3,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden'
                        }}>
                          {bid.proposal || 'No proposal provided'}
                        </Typography>
                      </Box>

                      <Divider />

                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Schedule color="action" />
                        <Typography variant="body2" color="text.secondary">
                          Bid placed: {formatDate(bid.createdAt)}
                        </Typography>
                      </Box>

                      {timeRemaining.expired ? (
                        <Chip label="Bidding Expired" color="error" size="small" />
                      ) : (
                        <Box>
                          <Typography variant="body2" color="text.secondary" gutterBottom>
                            Bidding ends in:
                          </Typography>
                          <CountdownTimer deadline={bid.taskBiddingDeadline} />
                        </Box>
                      )}
                    </Stack>
                  </CardContent>

                  <CardActions sx={{ p: 2, pt: 0 }}>
                    <Button
                      size="small"
                      startIcon={<Visibility />}
                      onClick={() => handleViewTask(bid.taskId)}
                    >
                      View Task
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      )}
    </Container>
  );
};

export default BidsPage;
