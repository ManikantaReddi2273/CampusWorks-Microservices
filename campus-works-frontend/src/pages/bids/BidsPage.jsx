import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  Gavel,
  Assignment
} from '@mui/icons-material';
import Layout from '@components/templates/Layout';
import BidCard from '@components/molecules/BidCard';
import { selectAuth } from '@store/slices/authSlice';
import { ROUTES } from '@constants';
import apiService from '@services/api';

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

  if (loading) {
    return (
      <Layout>
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4, display: 'flex', justifyContent: 'center' }}>
          <CircularProgress />
        </Container>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          <Alert severity="error">{error}</Alert>
        </Container>
      </Layout>
    );
  }

  return (
    <Layout>
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
          <Box sx={{ 
            display: 'flex', 
            gap: 2, 
            overflowX: 'auto',
            overflowY: 'hidden',
            pb: 2,
            '&::-webkit-scrollbar': {
              height: '8px',
            },
            '&::-webkit-scrollbar-track': {
              background: '#f1f1f1',
              borderRadius: '4px',
            },
            '&::-webkit-scrollbar-thumb': {
              background: '#888',
              borderRadius: '4px',
            },
            '&::-webkit-scrollbar-thumb:hover': {
              background: '#555',
            }
          }}>
            {bids.map((bid) => (
              <BidCard
                key={bid.id}
                bid={bid}
                variant="compact"
                onView={handleViewTask}
              />
            ))}
          </Box>
        )}
      </Container>
    </Layout>
  );
};

export default BidsPage;
