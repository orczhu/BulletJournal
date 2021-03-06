// page display contents of tasks
// react imports
import React, { useState } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import { connect } from 'react-redux';
// features
//actions
import {
  deleteTask,
  getTask,
  updateTaskContents,
} from '../../features/tasks/actions';
import { Label } from '../../features/label/interface';
import { addSelectedLabel } from '../../features/label/actions';
import { IState } from '../../store';
// antd imports
import {Avatar, Popconfirm, Tooltip, Button, Popover} from 'antd';
import {
  DeleteTwoTone,
  UpSquareOutlined,
  PlusCircleTwoTone,
} from '@ant-design/icons';
// modals import
import EditTask from '../../components/modals/edit-task.component';
import MoveProjectItem from '../../components/modals/move-project-item.component';
import ShareProjectItem from '../../components/modals/share-project-item.component';

import './task-page.styles.less';
import 'braft-editor/dist/index.css';
import { ProjectType } from '../../features/project/constants';
// components
import TaskDetailPage, { TaskProps } from './task-detail.pages';
import ContentEditorDrawer from '../../components/content-editor/content-editor-drawer.component';
import LabelManagement from "../project/label-management.compoent";

interface TaskPageHandler {
  getTask: (taskId: number) => void;
  addSelectedLabel: (label: Label) => void;
  deleteTask: (taskId: number) => void;
  updateTaskContents: (taskId: number) => void;
}

const TaskPage: React.FC<TaskPageHandler & TaskProps> = (props) => {
  const { task, deleteTask, updateTaskContents, contents } = props;
  // get id of task from router
  const { taskId } = useParams();
  // state control drawer displaying
  const [showEditor, setEditorShow] = useState(false);
  const [labelEditable, setLabelEditable] = useState(false);
  // hook history in router
  const history = useHistory();

  // listening on the empty state working as componentDidmount
  React.useEffect(() => {
    taskId && props.getTask(parseInt(taskId));
  }, [taskId]);

  React.useEffect(() => {
    task && task.id && updateTaskContents(task.id);
  }, [task]);
  // show drawer
  const createHandler = () => {
    setEditorShow(true);
  };

  const handleClose = () => {
    setEditorShow(false);
  };

  const labelEditableHandler = () => {
    setLabelEditable((labelEditable) => !labelEditable);
  };

  const createContentElem = (
    <Button onClick={createHandler}>
      <PlusCircleTwoTone />
      New
    </Button>
  );

  const taskEditorElem = (
    <div className='task-drawer'>
      <ContentEditorDrawer
        readMode={false}
        projectItem={task}
        visible={showEditor}
        onClose={handleClose}
      />
    </div>
  );

    const getAssignees = () => {
        if (!task.assignees || task.assignees.length === 0) {
            return null;
        }

        if (task.assignees.length === 1) {
            return         <Tooltip title={`Assignee ${task.assignees[0].name}`}>
                <div className='task-owner'>
                    <Avatar src={task.assignees[0].avatar} />
                </div>
            </Tooltip>
        }

        return  <Popover
            title='Assignees'
            placement='bottom'
            content={
                <div>
                    {task.assignees.map((u, index) => <p key={index}><Avatar size="small" src={u.avatar}/>&nbsp;{u.name}</p>)}
                </div>
            }
        >
            <div className='task-owner'>
                <Avatar src={task.assignees[0].avatar} />
            </div>
        </Popover>
    };

  const taskOperation = () => {
    return (
      <div className='task-operation'>
        {getAssignees()}

        <LabelManagement labelEditableHandler={labelEditableHandler} labelEditable={labelEditable}/>
        <EditTask task={task} mode='icon' />
        <MoveProjectItem
          type={ProjectType.TODO}
          projectItemId={task.id}
          mode='icon'
        />
        <ShareProjectItem
          type={ProjectType.TODO}
          projectItemId={task.id}
          mode='icon'
        />
        <Tooltip title='Delete'>
          <Popconfirm
            title='Deleting Task also deletes its child tasks. Are you sure?'
            okText='Yes'
            cancelText='No'
            onConfirm={() => {
              deleteTask(task.id);
              history.goBack();
            }}
            className='group-setting'
            placement='bottom'
          >
            <div>
              <DeleteTwoTone twoToneColor='#f5222d' />
            </div>
          </Popconfirm>
        </Tooltip>
        <Tooltip title='Go to Parent BuJo'>
          <div>
            <UpSquareOutlined
              onClick={(e) => history.push(`/projects/${task.projectId}`)}
            />
          </div>
        </Tooltip>
      </div>
    );
  };

  return (
    <TaskDetailPage
      task={task}
      labelEditable={labelEditable}
      taskOperation={taskOperation}
      contents={contents}
      createContentElem={createContentElem}
      taskEditorElem={taskEditorElem}
    />
  );
};

const mapStateToProps = (state: IState) => ({
  task: state.task.task,
  contents: state.task.contents,
});

export default connect(mapStateToProps, {
  deleteTask,
  getTask,
  addSelectedLabel,
  updateTaskContents,
})(TaskPage);
