import React from 'react';
import { ExportOutlined, SettingOutlined } from '@ant-design/icons';
import { Menu } from 'antd';
import { logoutUser } from '../../apis/myselfApis';
import { History } from 'history';

import './dropdown-menu.component.styles.less';

const handleLogout = () => {
  logoutUser().then(res => {
    window.location.href = res.headers.get('Location')!;
  });
};

type menuProps = {
  theme: string;
  username: string;
  history: History<History.PoorMansUnknown>;
};

const onClickSetting = (history: History<History.PoorMansUnknown>) => {
  history.push('/settings');
};

const DropdownMenu = ({ username, history, theme }: menuProps) => (
  <Menu theme={theme === 'DARK' ? 'dark' : 'light'} selectable={false}>
    <Menu.Item className='modified-item' style={{ cursor: 'default' }}>{username}</Menu.Item>
    <Menu.Item className='modified-item' onClick={() => onClickSetting(history)}>
      <SettingOutlined />
      Settings
    </Menu.Item>
    <Menu.Item className='modified-item' onClick={() => handleLogout()}>
      <ExportOutlined />
      Log Out
    </Menu.Item>
  </Menu>
);

export default DropdownMenu;
