import React, { useState, useEffect } from 'react';
import { Container, Typography, Box, Button, TextField, Dialog, DialogTitle, DialogContent, DialogActions, List, ListItem, ListItemText, IconButton, Chip, Stack, Paper, Divider, CircularProgress } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import AssessmentIcon from '@mui/icons-material/Assessment';
import LogoutIcon from '@mui/icons-material/Logout';
import './App.css';
import { Task, getAllTasks, addTask, updateTask, deleteTask } from './api';
import { useAuth } from './contexts/AuthContext';

function App() {
  const { user, signOut } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [editIndex, setEditIndex] = useState<number | null>(null);
  const [showReport, setShowReport] = useState(false);
  const [form, setForm] = useState({
    project: '',
    task: '',
    date: new Date().toISOString().slice(0, 10),
    hours: 1,
  });

  // Load tasks from API on mount
  useEffect(() => {
    async function fetchTasks() {
      setLoading(true);
      try {
        const data = await getAllTasks();
        setTasks(data);
      } catch (e) {
        alert('Failed to load tasks from API '+ (e as Error).message );
      }
      setLoading(false);
    }
    fetchTasks();
  }, []);

  const reloadTasks = async () => {
    setLoading(true);
    try {
      const data = await getAllTasks();
      setTasks(data);
    } catch (e) {
      alert('Failed to load tasks from API '+ (e as Error).message );
    }
    setLoading(false);
  };

  const handleOpen = (index: number | null = null) => {
    setEditIndex(index);
    if (index !== null) {
      const t = tasks[index];
      setForm({ project: t.project, task: t.task, date: t.date, hours: t.hours });
    } else {
      setForm({ project: '', task: '', date: new Date().toISOString().slice(0, 10), hours: 1 });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditIndex(null);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: Number(e.target.value) });
  };

  const handleSubmit = async () => {
    if (editIndex !== null) {
      const t = tasks[editIndex];
      await updateTask(t.id, { ...t, ...form });
    } else {
      await addTask({ ...form, id: Date.now() });
    }
    handleClose();
    reloadTasks();
  };

  const handleDelete = async (index: number) => {
    const t = tasks[index];
    await deleteTask(t.id);
    reloadTasks();
  };

  // Group by date for display
  const grouped = tasks.reduce<{ [date: string]: Task[] }>((acc, t) => {
    acc[t.date] = acc[t.date] || [];
    acc[t.date].push(t);
    return acc;
  }, {});

  // Get current month tasks and summary
  const getCurrentMonthTasks = () => {
    const now = new Date();
    const currentMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0');
    
    return tasks.filter(task => task.date.startsWith(currentMonth));
  };

  const getMonthlySummary = () => {
    const monthTasks = getCurrentMonthTasks();
    const projectSummary = monthTasks.reduce((acc, task) => {
      acc[task.project] = (acc[task.project] || 0) + task.hours;
      return acc;
    }, {} as { [key: string]: number });
    
    const totalHours = monthTasks.reduce((sum, task) => sum + task.hours, 0);
    
    return { projectSummary, totalHours, taskCount: monthTasks.length };
  };

  const currentMonthTasks = getCurrentMonthTasks();
  const monthlySummary = getMonthlySummary();

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h3" fontWeight={700} color="primary">
          Time Tracking
        </Typography>
        <Box display="flex" alignItems="center" gap={2}>
          {user && (
            <Typography variant="body2" color="text.secondary">
              Welcome, {user.username}!
            </Typography>
          )}
          <Button 
            variant="outlined" 
            color="error" 
            startIcon={<LogoutIcon />} 
            onClick={signOut}
            size="small"
          >
            Sign Out
          </Button>
        </Box>
      </Box>
      <Typography variant="subtitle1" align="center" color="text.secondary" gutterBottom>
        Track your daily work by project and task. Stay productive!
      </Typography>
      <Box display="flex" justifyContent="center" gap={2} mb={3}>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={() => handleOpen()} disabled={loading}>
          Add Task
        </Button>
        <Button 
          variant="outlined" 
          color="primary" 
          startIcon={<AssessmentIcon />} 
          onClick={() => setShowReport(!showReport)}
          disabled={loading}
        >
          {showReport ? 'Hide Report' : 'Monthly Report'}
        </Button>
      </Box>

      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="40vh">
          <CircularProgress size={60} color="primary" />
        </Box>
      ) : (
        <>
          {/* Monthly Report Section */}
          {showReport && (
            <Paper elevation={2} sx={{ p: 3, mb: 4, backgroundColor: '#f8f9fa' }}>
              <Typography variant="h5" fontWeight={600} color="primary" gutterBottom>
                Monthly Report - {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
              </Typography>
              
              {/* Summary Cards */}
              <Box display="flex" gap={2} mb={3} flexWrap="wrap">
                <Paper elevation={1} sx={{ p: 2, minWidth: 120, textAlign: 'center', backgroundColor: '#e3f2fd' }}>
                  <Typography variant="h6" color="primary" fontWeight={600}>
                    {monthlySummary.totalHours.toFixed(1)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Hours
                  </Typography>
                </Paper>
                <Paper elevation={1} sx={{ p: 2, minWidth: 120, textAlign: 'center', backgroundColor: '#f3e5f5' }}>
                  <Typography variant="h6" color="secondary" fontWeight={600}>
                    {monthlySummary.taskCount}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Tasks
                  </Typography>
                </Paper>
                <Paper elevation={1} sx={{ p: 2, minWidth: 120, textAlign: 'center', backgroundColor: '#e8f5e8' }}>
                  <Typography variant="h6" color="success.main" fontWeight={600}>
                    {Object.keys(monthlySummary.projectSummary).length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Projects
                  </Typography>
                </Paper>
              </Box>

              {/* Project Summary */}
              <Typography variant="h6" fontWeight={600} gutterBottom>
                Hours by Project
              </Typography>
              <Box mb={3}>
                {Object.entries(monthlySummary.projectSummary)
                  .sort(([,a], [,b]) => b - a)
                  .map(([project, hours]) => (
                    <Box key={project} display="flex" justifyContent="space-between" alignItems="center" py={1}>
                      <Typography variant="body1" fontWeight={500} color="primary">
                        {project}
                      </Typography>
                      <Chip label={`${hours.toFixed(1)}h`} color="success" size="small" />
                    </Box>
                  ))}
              </Box>

              <Divider sx={{ my: 2 }} />

              {/* All Month Tasks */}
              <Typography variant="h6" fontWeight={600} gutterBottom>
                All Tasks This Month
              </Typography>
              {currentMonthTasks.length === 0 ? (
                <Typography color="text.disabled" align="center" py={2}>
                  No tasks for this month yet.
                </Typography>
              ) : (
                <List sx={{ backgroundColor: 'white', borderRadius: 1 }}>
                  {currentMonthTasks
                    .sort((a, b) => b.date.localeCompare(a.date))
                    .map((task) => (
                      <ListItem key={task.id} divider>
                        <ListItemText
                          primary={
                            <Box display="flex" justifyContent="space-between" alignItems="center">
                              <Box>
                                <b style={{ color: '#1976d2' }}>{task.project}</b> — <span style={{ color: '#43a047' }}>{task.task}</span>
                              </Box>
                              <Chip label={`${task.hours}h`} color="warning" size="small" />
                            </Box>
                          }
                          secondary={
                            <Typography variant="body2" color="text.secondary">
                              {new Date(task.date).toLocaleDateString('en-US', { 
                                weekday: 'short', 
                                month: 'short', 
                                day: 'numeric' 
                              })}
                            </Typography>
                          }
                        />
                      </ListItem>
                    ))}
                </List>
              )}
            </Paper>
          )}

          {/* Existing Tasks Section */}
          <Box>
            {Object.keys(grouped).length === 0 && (
              <Typography align="center" color="text.disabled">No tasks yet. Add your first!</Typography>
            )}
            {Object.entries(grouped).sort((a, b) => b[0].localeCompare(a[0])).map(([date, dayTasks]) => (
              <Box key={date} mb={4}>
                <Chip label={date} color="primary" sx={{ fontWeight: 600, fontSize: 16, mb: 1 }} />
                <List>
                  {(dayTasks as Task[]).map((t, i) => (
                    <ListItem key={t.id} secondaryAction={
                      <Stack direction="row" spacing={1}>
                        <IconButton edge="end" aria-label="edit" color="primary" onClick={() => handleOpen(tasks.findIndex(tsk => tsk.id === t.id))} disabled={loading}>
                          <EditIcon />
                        </IconButton>
                        <IconButton edge="end" aria-label="delete" color="error" onClick={() => handleDelete(tasks.findIndex(tsk => tsk.id === t.id))} disabled={loading}>
                          <DeleteIcon />
                        </IconButton>
                      </Stack>
                    }>
                      <ListItemText
                        primary={<>
                          <b style={{ color: '#1976d2' }}>{t.project}</b> — <span style={{ color: '#43a047' }}>{t.task}</span>
                        </>}
                        secondary={<>
                          <span style={{ color: '#fbc02d', fontWeight: 600 }}>{t.hours}h</span>
                        </>}
                      />
                    </ListItem>
                  ))}
                </List>
              </Box>
            ))}
          </Box>
        </>
      )}
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>{editIndex !== null ? 'Edit Task' : 'Add Task'}</DialogTitle>
        <DialogContent>
          <TextField
            margin="dense"
            label="Project"
            name="project"
            value={form.project}
            onChange={handleChange}
            fullWidth
            required
            color="primary"
            disabled={loading}
          />
          <TextField
            margin="dense"
            label="Task"
            name="task"
            value={form.task}
            onChange={handleChange}
            fullWidth
            required
            color="secondary"
            disabled={loading}
          />
          <TextField
            margin="dense"
            label="Date"
            name="date"
            type="date"
            value={form.date}
            onChange={handleChange}
            fullWidth
            required
            InputLabelProps={{ shrink: true }}
            disabled={loading}
          />
          <TextField
            margin="dense"
            label="Hours"
            name="hours"
            type="number"
            value={form.hours}
            onChange={handleNumberChange}
            fullWidth
            required
            inputProps={{ min: 0.25, step: 0.25 }}
            color="warning"
            disabled={loading}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="inherit" disabled={loading}>Cancel</Button>
          <Button onClick={handleSubmit} variant="contained" color="primary" disabled={loading}>
            {editIndex !== null ? 'Save' : 'Add'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}

export default App;
