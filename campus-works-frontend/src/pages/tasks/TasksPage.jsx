import React from 'react';
import { Container, Typography, Paper } from '@mui/material';

const TasksPage = () => {
  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Tasks
      </Typography>
      
      <Paper sx={{ p: 3 }}>
        <Typography variant="body1">
          Tasks page - Coming soon in the next development phase.
        </Typography>
      </Paper>
    </Container>
  );
};

export default TasksPage;
