import { createSlice, PayloadAction } from 'redux-starter-kit';
import { ProjectItems } from './interface';

export type MyBuJo = {
  startDate: string;
  endDate: string;
};

export type SelectedCalendarDayAction = {
  selectedCalendarDay: string;
}

export type UpdateSelectedAction = {
  todoSelected: boolean;
  ledgerSelected: boolean;
};

export type ApiErrorAction = {
  error: string;
};

export type GetProjectItemsAction = {
  startDate: string;
  endDate: string;
  timezone: string;
  category: string;
};

export type GetProjectItemsAfterUpdateSelectAction = {
  todoSelected: boolean;
  ledgerSelected: boolean;
  category: string;
};

export type ProjectItemsReceivedAction = {
  items: ProjectItems[];
};

export type CalendarModeAction = {
  calendarMode: string;
};

let initialState = {
  startDate: '',
  endDate: '',
  projectItems: [] as ProjectItems[],
  todoSelected: true,
  ledgerSelected: false,
  calendarMode: 'month',
  selectedCalendarDay: '',
  projectItemsForCalendar: [] as ProjectItems[],
};

const slice = createSlice({
  name: 'myBuJo',
  initialState,
  reducers: {
    projectItemsApiErrorReceived: (
      state,
      action: PayloadAction<ApiErrorAction>
    ) => state,
    datesReceived: (state, action: PayloadAction<MyBuJo>) => {
      const { startDate, endDate } = action.payload;
      state.startDate = startDate;
      state.endDate = endDate;
    },
    selectedCalendarDayReceived: (state, action: PayloadAction<SelectedCalendarDayAction>) => {
      const { selectedCalendarDay } = action.payload;
      state.selectedCalendarDay = selectedCalendarDay;
    },
    updateSelected: (state, action: PayloadAction<UpdateSelectedAction>) => {
      const { todoSelected, ledgerSelected } = action.payload;
      state.todoSelected = todoSelected;
      state.ledgerSelected = ledgerSelected;
    },
    getProjectItems: (state, action: PayloadAction<GetProjectItemsAction>) =>
      state,
    getProjectItemsAfterUpdateSelect: (
      state,
      action: PayloadAction<GetProjectItemsAfterUpdateSelectAction>
    ) => state,
    projectItemsReceived: (
      state,
      action: PayloadAction<ProjectItemsReceivedAction>
    ) => {
      const { items } = action.payload;
      state.projectItems = items;
    },
    projectItemsForCalenderReceived: (
      state,
      action: PayloadAction<ProjectItemsReceivedAction>
    ) => {
      const { items } = action.payload;
      state.projectItemsForCalendar = items;
    },
    calendarModeReceived: (
      state,
      action: PayloadAction<CalendarModeAction>
    ) => {
      const { calendarMode } = action.payload;
      state.calendarMode = calendarMode;
    },
  }
});

export const reducer = slice.reducer;
export const actions = slice.actions;
