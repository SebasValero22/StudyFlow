use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TimerConfig {
    pub mode: String,
    pub focus_minutes: u32,
    pub short_break_minutes: u32,
    pub long_break_minutes: u32,
    pub cycles_before_long: u32,
}

impl Default for TimerConfig {
    fn default() -> Self {
        Self {
            mode: "ANIMEDORO".to_string(),
            focus_minutes: 40,
            short_break_minutes: 20,
            long_break_minutes: 30,
            cycles_before_long: 4,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub enum TimerState {
    Idle,
    Focus {
        remaining_secs: u32,
        total_secs: u32,
        task_id: Option<i32>,
        task_title: Option<String>,
        task_color: Option<String>,
        cycle: u32,
    },
    ShortBreak {
        remaining_secs: u32,
        total_secs: u32,
        cycle: u32,
    },
    LongBreak {
        remaining_secs: u32,
        total_secs: u32,
    },
    Paused {
        previous: Box<TimerState>,
    },
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum TimerEvent {
    Tick,
    SessionComplete {
        mode: String,
        duration_secs: u32,
        task_id: Option<i32>,
    },
    PhaseChange {
        new_mode: String,
    },
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct StatusResponse {
    pub state: String,
    pub remaining_seconds: u32,
    pub total_seconds: u32,
    pub task_title: Option<String>,
    pub task_color: Option<String>,
    pub session_type: String,
    pub cycle: String,
    pub enabled: bool,
}

pub struct Timer {
    pub state: TimerState,
    pub config: TimerConfig,
    pub current_cycle: u32,
}

impl Timer {
    pub fn new(config: TimerConfig) -> Self {
        Self {
            state: TimerState::Idle,
            config,
            current_cycle: 1,
        }
    }

    pub fn start(&mut self, task_id: Option<i32>, task_title: Option<String>, task_color: Option<String>) {
        let focus_secs = self.config.focus_minutes * 60;
        self.state = TimerState::Focus {
            remaining_secs: focus_secs,
            total_secs: focus_secs,
            task_id,
            task_title,
            task_color,
            cycle: self.current_cycle,
        };
    }

    pub fn pause(&mut self) {
        if let TimerState::Paused { .. } = self.state {
            return;
        }
        if matches!(self.state, TimerState::Idle) {
            return;
        }
        let current_state = std::mem::replace(&mut self.state, TimerState::Idle);
        self.state = TimerState::Paused {
            previous: Box::new(current_state),
        };
    }

    pub fn resume(&mut self) {
        if let TimerState::Paused { previous } = &self.state {
            self.state = *previous.clone();
        }
    }

    pub fn stop(&mut self) {
        self.state = TimerState::Idle;
        self.current_cycle = 1;
    }

    pub fn skip(&mut self) -> Option<TimerEvent> {
        match &self.state {
            TimerState::Focus { task_id, .. } => {
                let tid = *task_id;
                self.transition_from_focus();
                Some(TimerEvent::SessionComplete {
                    mode: "FOCUS".to_string(),
                    duration_secs: self.config.focus_minutes * 60,
                    task_id: tid,
                })
            }
            TimerState::ShortBreak { .. } | TimerState::LongBreak { .. } => {
                self.transition_from_break();
                Some(TimerEvent::PhaseChange { new_mode: "IDLE".to_string() })
            }
            _ => None,
        }
    }

    pub fn tick(&mut self) -> Option<TimerEvent> {
        let mut event = None;

        self.state = match &self.state {
            TimerState::Focus { remaining_secs, total_secs, task_id, task_title, task_color, cycle } => {
                if *remaining_secs > 0 {
                    event = Some(TimerEvent::Tick);
                    TimerState::Focus {
                        remaining_secs: remaining_secs - 1,
                        total_secs: *total_secs,
                        task_id: *task_id,
                        task_title: task_title.clone(),
                        task_color: task_color.clone(),
                        cycle: *cycle,
                    }
                } else {
                    event = Some(TimerEvent::SessionComplete {
                        mode: "FOCUS".to_string(),
                        duration_secs: *total_secs,
                        task_id: *task_id,
                    });
                    if *cycle >= self.config.cycles_before_long {
                        TimerState::LongBreak {
                            remaining_secs: self.config.long_break_minutes * 60,
                            total_secs: self.config.long_break_minutes * 60,
                        }
                    } else {
                        TimerState::ShortBreak {
                            remaining_secs: self.config.short_break_minutes * 60,
                            total_secs: self.config.short_break_minutes * 60,
                            cycle: *cycle,
                        }
                    }
                }
            }
            TimerState::ShortBreak { remaining_secs, total_secs, cycle } => {
                if *remaining_secs > 0 {
                    event = Some(TimerEvent::Tick);
                    TimerState::ShortBreak { remaining_secs: remaining_secs - 1, total_secs: *total_secs, cycle: *cycle }
                } else {
                    event = Some(TimerEvent::PhaseChange { new_mode: "IDLE".to_string() });
                    self.current_cycle += 1;
                    TimerState::Idle
                }
            }
            TimerState::LongBreak { remaining_secs, total_secs } => {
                if *remaining_secs > 0 {
                    event = Some(TimerEvent::Tick);
                    TimerState::LongBreak { remaining_secs: remaining_secs - 1, total_secs: *total_secs }
                } else {
                    event = Some(TimerEvent::PhaseChange { new_mode: "IDLE".to_string() });
                    self.current_cycle = 1;
                    TimerState::Idle
                }
            }
            _ => self.state.clone(),
        };

        event
    }

    fn transition_from_focus(&mut self) {
        if self.current_cycle >= self.config.cycles_before_long {
            self.state = TimerState::LongBreak {
                remaining_secs: self.config.long_break_minutes * 60,
                total_secs: self.config.long_break_minutes * 60,
            };
        } else {
            self.state = TimerState::ShortBreak {
                remaining_secs: self.config.short_break_minutes * 60,
                total_secs: self.config.short_break_minutes * 60,
                cycle: self.current_cycle,
            };
        }
    }

    fn transition_from_break(&mut self) {
        if let TimerState::LongBreak { .. } = self.state {
            self.current_cycle = 1;
        } else {
            self.current_cycle += 1;
        }
        self.state = TimerState::Idle;
    }

    pub fn status(&self) -> StatusResponse {
        let (state_str, rem_secs, tot_secs, t_title, t_color, s_type, cycle_str) = match &self.state {
            TimerState::Idle => ("idle", 0, 0, None, None, "NONE", format!("{}/{}", self.current_cycle, self.config.cycles_before_long)),
            TimerState::Focus { remaining_secs, total_secs, task_title, task_color, cycle, .. } => 
                ("focus", *remaining_secs, *total_secs, task_title.clone(), task_color.clone(), "FOCUS", format!("{}/{}", cycle, self.config.cycles_before_long)),
            TimerState::ShortBreak { remaining_secs, total_secs, cycle } => 
                ("short_break", *remaining_secs, *total_secs, None, None, "BREAK", format!("{}/{}", cycle, self.config.cycles_before_long)),
            TimerState::LongBreak { remaining_secs, total_secs } => 
                ("long_break", *remaining_secs, *total_secs, None, None, "BREAK", format!("{}/{}", self.config.cycles_before_long, self.config.cycles_before_long)),
            TimerState::Paused { previous } => {
                match &**previous {
                    TimerState::Focus { remaining_secs, total_secs, task_title, task_color, cycle, .. } =>
                        ("paused", *remaining_secs, *total_secs, task_title.clone(), task_color.clone(), "FOCUS", format!("{}/{}", cycle, self.config.cycles_before_long)),
                    TimerState::ShortBreak { remaining_secs, total_secs, cycle } =>
                        ("paused", *remaining_secs, *total_secs, None, None, "BREAK", format!("{}/{}", cycle, self.config.cycles_before_long)),
                    TimerState::LongBreak { remaining_secs, total_secs } =>
                        ("paused", *remaining_secs, *total_secs, None, None, "BREAK", format!("{}/{}", self.config.cycles_before_long, self.config.cycles_before_long)),
                    _ => ("paused", 0, 0, None, None, "NONE", "0/0".to_string()),
                }
            }
        };

        StatusResponse {
            state: state_str.to_string(),
            remaining_seconds: rem_secs,
            total_seconds: tot_secs,
            task_title: t_title,
            task_color: t_color,
            session_type: s_type.to_string(),
            cycle: cycle_str,
            enabled: true,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_timer_lifecycle() {
        let config = TimerConfig {
            mode: "ANIMEDORO".to_string(),
            focus_minutes: 1,
            short_break_minutes: 1,
            long_break_minutes: 2,
            cycles_before_long: 2,
        };
        let mut timer = Timer::new(config);

        assert_eq!(timer.status().state, "idle");

        timer.start(Some(10), Some("Math Review".to_string()), Some("#4F46E5".to_string()));
        let st = timer.status();
        assert_eq!(st.state, "focus");
        assert_eq!(st.remaining_seconds, 60);
        assert_eq!(st.task_title, Some("Math Review".to_string()));

        timer.pause();
        assert_eq!(timer.status().state, "paused");

        timer.resume();
        assert_eq!(timer.status().state, "focus");

        let event = timer.tick();
        assert_eq!(event, Some(TimerEvent::Tick));
        assert_eq!(timer.status().remaining_seconds, 59);

        timer.stop();
        assert_eq!(timer.status().state, "idle");
    }
}
