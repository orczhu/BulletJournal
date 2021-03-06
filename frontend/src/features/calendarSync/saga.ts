import { all, call, put, takeLatest, select } from 'redux-saga/effects';
import { message } from 'antd';
import {
  actions as calendarSyncActions,
  GoogleCalendarCreateEventsAction,
  UnwatchedCalendarAction,
  UpdateExpirationTimeAction,
  UpdateGoogleCalendarEventListAction,
  UpdateWatchedProjectAction,
  WatchedCalendarAction,
  ImportEventsToProjectAction,
} from './reducer';
import { PayloadAction } from 'redux-starter-kit';
import {
  createGoogleCalendarEvents,
  getGoogleCalendarEventList,
  getGoogleCalendarList,
  getGoogleCalendarLoginStatus,
  getWatchedProject,
  unwatchCalendar,
  watchCalendar,
  importEventsToProject as importEventsApi,
} from '../../apis/calendarApis';
import {
  CalendarListEntry,
  LoginStatus,
  GoogleCalendarEvent,
} from './interface';
import { Project } from '../project/interface';
import { IState } from '../../store';

function* googleTokenExpirationTimeUpdate(
  action: PayloadAction<UpdateExpirationTimeAction>
) {
  try {
    const loginStatus: LoginStatus = yield call(getGoogleCalendarLoginStatus);
    const expirationTime = loginStatus.loggedIn
      ? loginStatus.expirationTime
      : 0;
    yield put(
      calendarSyncActions.googleTokenExpirationTimeReceived({
        expirationTime: expirationTime,
      })
    );
    if (expirationTime) {
      const calendarList: CalendarListEntry[] = yield call(
        getGoogleCalendarList
      );
      yield put(
        calendarSyncActions.googleCalendarListReceived({
          calendarList: calendarList,
        })
      );
    }
  } catch (error) {
    yield call(
      message.error,
      `googleTokenExpirationTimeUpdate Error Received: ${error}`
    );
  }
}

function* googleCalendarEventListUpdate(
  action: PayloadAction<UpdateGoogleCalendarEventListAction>
) {
  try {
    const { calendarId, timezone, startDate, endDate } = action.payload;
    const data = yield call(
      getGoogleCalendarEventList,
      calendarId,
      timezone,
      startDate,
      endDate
    );
    yield put(
      calendarSyncActions.googleCalendarEventListReceived({
        googleCalendarEventList: data,
      })
    );
  } catch (error) {
    yield call(
      message.error,
      `googleCalendarEventListUpdate Error Received: ${error}`
    );
  }
}

function* googleCalendarCreateEvents(
  action: PayloadAction<GoogleCalendarCreateEventsAction>
) {
  try {
    const { projectId, events } = action.payload;
    yield call(createGoogleCalendarEvents, projectId, events);
  } catch (error) {
    yield call(
      message.error,
      `googleCalendarCreateEvents Error Received: ${error}`
    );
  }
}

function* updateWatchedProject(
  action: PayloadAction<UpdateWatchedProjectAction>
) {
  try {
    const { calendarId } = action.payload;
    const project: Project = yield call(getWatchedProject, calendarId);
    yield put(
      calendarSyncActions.watchedProjectReceived({
        project: project && project.id ? project : undefined,
      })
    );
  } catch (error) {
    yield call(message.error, `updateWatchedProject Error Received: ${error}`);
  }
}

function* watchCalendarChannel(action: PayloadAction<WatchedCalendarAction>) {
  try {
    const { calendarId, projectId } = action.payload;
    const project: Project = yield call(watchCalendar, calendarId, projectId);
    yield put(calendarSyncActions.watchedProjectReceived({ project: project }));
  } catch (error) {
    yield call(message.error, `watchCalendarChannel Error Received: ${error}`);
  }
}

function* unwatchCalendarChannel(
  action: PayloadAction<UnwatchedCalendarAction>
) {
  try {
    const { calendarId } = action.payload;
    yield call(unwatchCalendar, calendarId);
    yield put(
      calendarSyncActions.watchedProjectReceived({ project: undefined })
    );
  } catch (error) {
    yield call(message.error, `watchCalendarChannel Error Received: ${error}`);
  }
}

function* importEventsToProject(
  action: PayloadAction<ImportEventsToProjectAction>
) {
  try {
    const { projectId, eventList } = action.payload;
    const state: IState = yield select();
    let importEvents = [] as GoogleCalendarEvent[];
    const allEvents = state.calendarSync.googleCalendarEventList;
    if (!allEvents || allEvents.length <= 0) return;

    eventList.forEach((eventId: string) => {
      let event = allEvents.filter(
        (e: GoogleCalendarEvent) => e.iCalUID === eventId
      );
      importEvents.push(event[0]);
    });

    yield call(importEventsApi, projectId, importEvents);
  } catch (error) {
    yield call(message.error, `watchCalendarChannel Error Received: ${error}`);
  }
}

export default function* calendarSyncSagas() {
  yield all([
    yield takeLatest(
      calendarSyncActions.googleTokenExpirationTimeUpdate.type,
      googleTokenExpirationTimeUpdate
    ),
    yield takeLatest(
      calendarSyncActions.googleCalendarEventListUpdate.type,
      googleCalendarEventListUpdate
    ),
    yield takeLatest(
      calendarSyncActions.googleCalendarCreateEvents.type,
      googleCalendarCreateEvents
    ),
    yield takeLatest(
      calendarSyncActions.watchedProjectUpdate.type,
      updateWatchedProject
    ),
    yield takeLatest(
      calendarSyncActions.watchCalendar.type,
      watchCalendarChannel
    ),
    yield takeLatest(
      calendarSyncActions.unwatchCalendar.type,
      unwatchCalendarChannel
    ),
    yield takeLatest(
      calendarSyncActions.importEventsToProject.type,
      importEventsToProject
    ),
  ]);
}
