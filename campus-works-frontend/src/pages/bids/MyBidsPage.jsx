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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  DialogContentText
} from '@mui/material';
import {
  Gavel,
  Visibility,
  Assignment,
  AttachMoney,
  Schedule,
  Person,
  Refresh,
  TrendingUp,
  CheckCircle,
  Cancel,
  Pending,
  TaskAlt,
  Payment,
  Delete
} from '@mui/icons-material';
import Layout from '@components/templates/Layout';
import { selectAuth } from '@store/slices/authSlice';
import { ROUTES, CATEGORY_LABELS } from '@constants';
import apiService from '@services/api';
import { isDeadlineExpired, getDeadlineWarning, getDeadlineStatusColor } from '@utils/deadlineUtils';
import CountdownTimer from '@components/common/CountdownTimer';

const MyBidsPage = () => {
  const navigate = useNavigate();
  const { user } = useSelector(selectAuth);

  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deletingBid, setDeletingBid] = useState(null);
  
  // UPI ID submission state
  const [upiDialogOpen, setUpiDialogOpen] = useState(false);
  const [selectedBid, setSelectedBid] = useState(null);
  const [upiId, setUpiId] = useState('');
  const [submittingUpi, setSubmittingUpi] = useState(false);
  const [upiError, setUpiError] = useState('');
  const [loadingTaskDetails, setLoadingTaskDetails] = useState(false);

  useEffect(() => {
    if (user?.email) {
      fetchMyBids();
    }
  }, [user?.email]);

  const fetchMyBids = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await apiService.bids.getByUserEmail(user.email);
      console.log('Bids response:', response);
      console.log('Bids data:', response.data);
      
      const bidsData = response.data || [];
      console.log('Processed bids:', bidsData);
      
      // Log each bid's taskId
      bidsData.forEach((bid, index) => {
        console.log(`Bid ${index}:`, {
          id: bid.id,
          taskId: bid.taskId,
          status: bid.status,
          amount: bid.amount
        });
      });
      
      setBids(bidsData);
      
    } catch (error) {
      console.error('Error fetching my bids:', error);
      setError('Failed to load your bids. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleViewTask = (taskId) => {
    console.log('Viewing task with ID:', taskId);
    if (!taskId) {
      console.error('No task ID provided for viewing');
      setError('Cannot view task: No task ID provided');
      return;
    }
    navigate(`${ROUTES.TASK_DETAIL.replace(':id', taskId)}`);
  };

  const handleDeleteBid = async (bidId) => {
    if (window.confirm('Are you sure you want to delete this rejected bid? This action cannot be undone.')) {
      try {
        setDeletingBid(bidId);
        setError(null);
        
        await apiService.bids.delete(bidId);
        
        // Remove the bid from the local state
        setBids(bids.filter(bid => bid.id !== bidId));
        
      } catch (error) {
        console.error('Error deleting bid:', error);
        const errorMessage = error.response?.data?.message || error.message || 'Failed to delete bid.';
        setError(`Failed to delete bid: ${errorMessage}`);
      } finally {
        setDeletingBid(null);
      }
    }
  };

  const handleCompleteTask = async (bid) => {
    try {
      setSelectedBid(bid);
      setUpiId('');
      setUpiError('');
      setLoadingTaskDetails(true);
      
      // Fetch task details to populate the modal
      if (bid.taskId) {
        const taskResponse = await apiService.tasks.getById(bid.taskId);
        if (taskResponse && taskResponse.data) {
          // Update the selectedBid with task details
          const updatedBid = {
            ...bid,
            task: {
              title: taskResponse.data.title,
              ownerEmail: taskResponse.data.ownerEmail,
              budget: taskResponse.data.budget
            }
          };
          setSelectedBid(updatedBid);
        }
      }
      
      setUpiDialogOpen(true);
    } catch (error) {
      console.error('Error fetching task details:', error);
      setError('Failed to load task details. Please try again.');
    } finally {
      setLoadingTaskDetails(false);
    }
  };

  const handleSubmitUpiId = async () => {
    if (!upiId.trim()) {
      setUpiError('UPI ID is required');
      return;
    }

    if (upiId.length < 5 || upiId.length > 50) {
      setUpiError('UPI ID must be between 5 and 50 characters');
      return;
    }

    try {
      setSubmittingUpi(true);
      setUpiError('');

      const response = await apiService.bids.submitUpiId(selectedBid.id, { upiId: upiId.trim() });
      
      if (response.data) {
        // Update the bid in the local state
        setBids(prevBids => 
          prevBids.map(bid => 
            bid.id === selectedBid.id 
              ? { ...bid, upiId: upiId.trim(), upiIdSubmittedAt: new Date().toISOString() }
              : bid
          )
        );
        
        setUpiDialogOpen(false);
        setSelectedBid(null);
        setUpiId('');
      }
    } catch (error) {
      console.error('Error submitting UPI ID:', error);
      setUpiError(error.response?.data?.message || 'Failed to submit UPI ID. Please try again.');
    } finally {
      setSubmittingUpi(false);
    }
  };

  const handleCloseUpiDialog = () => {
    setUpiDialogOpen(false);
    setSelectedBid(null);
    setUpiId('');
    setUpiError('');
    setLoadingTaskDetails(false);
  };

  const canCompleteTask = (bid) => {
    return bid.status === 'ACCEPTED' && 
           !bid.upiId && 
           !isDeadlineExpired(bid.task?.completionDeadline);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'ACCEPTED':
        return 'success';
      case 'REJECTED':
        return 'error';
      case 'WITHDRAWN':
        return 'default';
      case 'COMPLETED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING':
        return <Pending />;
      case 'ACCEPTED':
        return <CheckCircle />;
      case 'REJECTED':
        return <Cancel />;
      case 'WITHDRAWN':
        return <Cancel />;
      case 'COMPLETED':
        return <TaskAlt />;
      case 'CANCELLED':
        return <Cancel />;
      default:
        return <Pending />;
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  if (loading) {
    return (
      <Layout>
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
            <CircularProgress size={60} />
          </Box>
        </Container>
      </Layout>
    );
  }

  return (
    <Box sx={{ 
      minHeight: '100vh',
      background: 'transparent',
      py: 4,
      px: 2
    }}>
      <Layout>
        <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
          {/* Header */}
          <Box sx={{ mb: 4 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
              <Typography variant="h4" component="h1">
                My Bids
              </Typography>
              <Button
                variant="outlined"
                startIcon={<Refresh />}
                onClick={fetchMyBids}
              >
                Refresh
              </Button>
            </Box>
            <Typography variant="body1" color="text.secondary">
              Track the status of all your submitted bids
            </Typography>
          </Box>

          {/* Error Alert */}
          {error && (
            <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
              {error}
            </Alert>
          )}

          {/* Bids List */}
          {bids.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center' }}>
              <Gavel sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h5" gutterBottom>
                No Bids Found
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                You haven't placed any bids yet. Start browsing tasks to find opportunities!
              </Typography>
              <Button
                variant="contained"
                startIcon={<Assignment />}
                onClick={() => navigate(ROUTES.TASKS)}
                sx={{ 
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%)',
                  }
                }}
              >
                Browse Tasks
              </Button>
            </Paper>
          ) : (
            <Grid container spacing={3}>
              {bids.map((bid) => (
                <Grid item xs={12} key={bid.id}>
                  <Card sx={{ 
                    height: '100%', 
                    display: 'flex', 
                    flexDirection: 'column',
                    background: 'rgba(255, 255, 255, 0.95)',
                    backdropFilter: 'blur(10px)',
                    border: '1px solid rgba(255, 255, 255, 0.2)'
                  }}>
                    <CardContent sx={{ flexGrow: 1 }}>
                      {/* Bid Header */}
                      <Box display="flex" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 2 }}>
                        <Box sx={{ flexGrow: 1 }}>
                                                     <Typography variant="h6" component="h2" sx={{ 
                             fontWeight: 'bold',
                             mb: 1
                           }}>
                             {bid.task?.title || 'Loading...'}
                           </Typography>
                          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 1 }}>
                            <Chip
                              icon={getStatusIcon(bid.status)}
                              label={bid.status}
                              color={getStatusColor(bid.status)}
                              size="small"
                              sx={{ fontWeight: 'bold' }}
                            />
                                                         {bid.task?.category && (
                               <Chip
                                 label={CATEGORY_LABELS[bid.task.category] || bid.task.category}
                                 size="small"
                                 variant="outlined"
                                 sx={{ 
                                   background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                   color: 'white',
                                   fontWeight: 'bold'
                                 }}
                               />
                             )}
                          </Box>
                        </Box>
                        <Box display="flex" alignItems="center" gap={1}>
                          <TrendingUp sx={{ color: 'success.main' }} />
                          <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'success.main' }}>
                            {formatCurrency(bid.amount)}
                          </Typography>
                        </Box>
                      </Box>

                      <Divider sx={{ my: 2 }} />

                      {/* Bid Details */}
                      <Grid container spacing={2}>
                        <Grid item xs={12} md={6}>
                          <Box sx={{ mb: 2 }}>
                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                              <AttachMoney sx={{ mr: 1, fontSize: 16 }} />
                              <strong>Your Bid:</strong> {formatCurrency(bid.amount)}
                            </Typography>
                                                         {bid.task?.budget && (
                               <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                 <AttachMoney sx={{ mr: 1, fontSize: 16 }} />
                                 <strong>Task Budget:</strong> {formatCurrency(bid.task.budget)}
                               </Typography>
                             )}
                             {bid.task?.ownerEmail && (
                               <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                 <Person sx={{ mr: 1, fontSize: 16 }} />
                                 <strong>Task Owner:</strong> {bid.task.ownerEmail}
                               </Typography>
                             )}
                          </Box>
                        </Grid>
                        <Grid item xs={12} md={6}>
                          <Box sx={{ mb: 2 }}>
                            <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                              <Schedule sx={{ mr: 1, fontSize: 16 }} />
                              <strong>Bid Placed:</strong> {formatDate(bid.createdAt)}
                            </Typography>
                            {bid.task?.completionDeadline && (
                              <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                <Schedule sx={{ mr: 1, fontSize: 16 }} />
                                <strong>Task Deadline:</strong> {formatDate(bid.task.completionDeadline)}
                              </Typography>
                            )}
                            
                            {/* Live Countdown Timer for Accepted Bids */}
                            {bid.status === 'ACCEPTED' && bid.task?.completionDeadline && !isDeadlineExpired(bid.task.completionDeadline) && (
                              <Box sx={{ mt: 1, mb: 1 }}>
                                <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 0.5, fontWeight: 'bold' }}>
                                  <Schedule sx={{ mr: 1, fontSize: 16 }} />
                                  Time Remaining:
                                </Typography>
                                <CountdownTimer 
                                  deadline={bid.task.completionDeadline}
                                  variant="compact"
                                  size="small"
                                />
                              </Box>
                            )}
                            {bid.updatedAt && bid.updatedAt !== bid.createdAt && (
                              <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                <Schedule sx={{ mr: 1, fontSize: 16 }} />
                                <strong>Last Updated:</strong> {formatDate(bid.updatedAt)}
                              </Typography>
                            )}
                            {bid.acceptedAt && (
                              <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1, color: 'success.main' }}>
                                <CheckCircle sx={{ mr: 1, fontSize: 16 }} />
                                <strong>Accepted At:</strong> {formatDate(bid.acceptedAt)}
                              </Typography>
                            )}
                            {bid.rejectedAt && (
                              <Typography variant="body2" sx={{ display: 'flex', alignItems: 'center', mb: 1, color: 'error.main' }}>
                                <Cancel sx={{ mr: 1, fontSize: 16 }} />
                                <strong>Rejected At:</strong> {formatDate(bid.rejectedAt)}
                              </Typography>
                            )}
                          </Box>
                        </Grid>
                      </Grid>

                      {/* Bid Proposal */}
                      {bid.proposal && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1 }}>
                            Your Proposal:
                          </Typography>
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                              p: 2,
                              backgroundColor: 'rgba(102, 126, 234, 0.05)',
                              borderRadius: 1,
                              border: '1px solid rgba(102, 126, 234, 0.1)',
                              fontStyle: 'italic'
                            }}
                          >
                            {bid.proposal}
                          </Typography>
                        </Box>
                      )}

                      {/* Rejection Reason */}
                      {bid.status === 'REJECTED' && bid.rejectionReason && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1, color: 'error.main' }}>
                            Rejection Reason:
                          </Typography>
                          <Typography
                            variant="body2"
                            color="error.main"
                            sx={{
                              p: 2,
                              backgroundColor: 'rgba(244, 67, 54, 0.05)',
                              borderRadius: 1,
                              border: '1px solid rgba(244, 67, 54, 0.1)',
                              fontStyle: 'italic'
                            }}
                          >
                            {bid.rejectionReason}
                          </Typography>
                        </Box>
                      )}

                      {/* Task Description */}
                      {bid.task?.description && (
                        <Box sx={{ mt: 2 }}>
                          <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1 }}>
                            Task Description:
                          </Typography>
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              display: '-webkit-box',
                              WebkitLineClamp: 3,
                              WebkitBoxOrient: 'vertical'
                            }}
                          >
                            {bid.task.description}
                          </Typography>
                        </Box>
                      )}
                    </CardContent>

                      {/* Bid Actions */}
                      <CardActions sx={{ p: 2, pt: 0 }}>
                        <Tooltip title={bid.taskId ? "View Task Details" : "Task ID not available"}>
                          <span>
                            <IconButton
                              size="small"
                              onClick={() => handleViewTask(bid.taskId)}
                              disabled={!bid.taskId}
                              sx={{
                                color: '#000000',
                                '&:hover': {
                                  color: '#1976d2',
                                  backgroundColor: 'rgba(25, 118, 210, 0.1)'
                                },
                                '&:disabled': {
                                  color: '#ccc'
                                }
                              }}
                            >
                              <Visibility />
                            </IconButton>
                          </span>
                        </Tooltip>
                        
                        {/* Delete Button for Rejected Bids */}
                        {bid.status === 'REJECTED' && (
                          <Tooltip title="Delete Rejected Bid">
                            <IconButton
                              size="small"
                              onClick={() => handleDeleteBid(bid.id)}
                              color="error"
                              disabled={deletingBid === bid.id}
                            >
                              {deletingBid === bid.id ? <CircularProgress size={16} /> : <Delete />}
                            </IconButton>
                          </Tooltip>
                        )}
                        
                        {/* Complete Task Button */}
                        {canCompleteTask(bid) && (
                          <Tooltip title="Complete Task - Submit UPI ID">
                            <Button
                              size="small"
                              variant="contained"
                              startIcon={<Payment />}
                              onClick={() => handleCompleteTask(bid)}
                              sx={{
                                background: 'linear-gradient(135deg, #4caf50 0%, #45a049 100%)',
                                '&:hover': {
                                  background: 'linear-gradient(135deg, #45a049 0%, #3d8b40 100%)',
                                }
                              }}
                            >
                              Complete Task
                            </Button>
                          </Tooltip>
                        )}
                        
                        {/* UPI ID Submitted Status */}
                        {bid.status === 'ACCEPTED' && bid.upiId && (
                          <Chip
                            icon={<Payment />}
                            label="UPI ID Submitted"
                            color="info"
                            size="small"
                            sx={{ ml: 1 }}
                          />
                        )}
                        
                        {/* Task Deadline Expired Warning */}
                        {bid.status === 'ACCEPTED' && isDeadlineExpired(bid.task?.completionDeadline) && (
                          <Chip
                            icon={<Schedule />}
                            label="Deadline Expired"
                            color="error"
                            size="small"
                            sx={{ ml: 1 }}
                          />
                        )}
                        
                        {/* Deadline Warning */}
                        {bid.status === 'ACCEPTED' && !isDeadlineExpired(bid.task?.completionDeadline) && getDeadlineWarning(bid.task?.completionDeadline) && (
                          <Chip
                            icon={<Schedule />}
                            label="Deadline Soon"
                            color={getDeadlineStatusColor(bid.task?.completionDeadline)}
                            size="small"
                            sx={{ ml: 1 }}
                          />
                        )}
                      </CardActions>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}

          {/* UPI ID Submission Dialog */}
          <Dialog open={upiDialogOpen} onClose={handleCloseUpiDialog} maxWidth="sm" fullWidth>
            <DialogTitle>
              <Box display="flex" alignItems="center" gap={1}>
                <Payment color="primary" />
                Complete Task - Submit UPI ID
              </Box>
            </DialogTitle>
            <DialogContent>
              <DialogContentText sx={{ mb: 2 }}>
                You are about to mark this task as complete. Please provide your UPI ID so the task owner can make the payment.
              </DialogContentText>
              
              {selectedBid && (
                <Box sx={{ mb: 2, p: 2, backgroundColor: 'rgba(102, 126, 234, 0.05)', borderRadius: 1 }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 'bold', mb: 1 }}>
                    Task Details:
                  </Typography>
                  {loadingTaskDetails ? (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CircularProgress size={16} />
                      <Typography variant="body2">Loading task details...</Typography>
                    </Box>
                  ) : (
                    <>
                      <Typography variant="body2">
                        <strong>Task:</strong> {selectedBid.task?.title || 'Loading...'}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Your Bid:</strong> {formatCurrency(selectedBid.amount)}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Task Owner:</strong> {selectedBid.task?.ownerEmail || 'Loading...'}
                      </Typography>
                    </>
                  )}
                </Box>
              )}
              
              <TextField
                autoFocus
                margin="dense"
                label="UPI ID"
                placeholder="Enter your UPI ID (e.g., yourname@paytm, yourname@phonepe)"
                fullWidth
                variant="outlined"
                value={upiId}
                onChange={(e) => setUpiId(e.target.value)}
                error={!!upiError}
                helperText={upiError || "Enter your UPI ID so the task owner can pay you"}
                disabled={submittingUpi}
              />
              
              <Alert severity="info" sx={{ mt: 2 }}>
                <Typography variant="body2">
                  <strong>Note:</strong> Once you submit your UPI ID, the task owner will be able to view it and make the payment. 
                  Make sure your UPI ID is correct and active.
                </Typography>
              </Alert>
              
              {selectedBid?.task?.completionDeadline && getDeadlineWarning(selectedBid.task.completionDeadline) && (
                <Alert severity={getDeadlineStatusColor(selectedBid.task.completionDeadline)} sx={{ mt: 2 }}>
                  <Typography variant="body2">
                    <strong>Deadline Alert:</strong> {getDeadlineWarning(selectedBid.task.completionDeadline)}
                  </Typography>
                </Alert>
              )}
            </DialogContent>
            <DialogActions sx={{ p: 3 }}>
              <Button onClick={handleCloseUpiDialog} disabled={submittingUpi}>
                Cancel
              </Button>
              <Button
                onClick={handleSubmitUpiId}
                variant="contained"
                disabled={submittingUpi || !upiId.trim()}
                startIcon={submittingUpi ? <CircularProgress size={20} /> : <Payment />}
                sx={{
                  background: 'linear-gradient(135deg, #4caf50 0%, #45a049 100%)',
                  '&:hover': {
                    background: 'linear-gradient(135deg, #45a049 0%, #3d8b40 100%)',
                  }
                }}
              >
                {submittingUpi ? 'Submitting...' : 'Submit UPI ID'}
              </Button>
            </DialogActions>
          </Dialog>
        </Container>
      </Layout>
    </Box>
  );
};

export default MyBidsPage;
