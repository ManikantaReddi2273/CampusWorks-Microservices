import React from 'react';
import { Container, Typography, Paper } from '@mui/material';

const TaskDetailPage = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Task Details
      </Typography>
      
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1">
          Task detail page - Coming soon in the next development phase.
        </Typography>
      </Paper>
    </Container>
  );
};

export default TaskDetailPage;
