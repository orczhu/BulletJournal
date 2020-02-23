import React from 'react';
import TimezonePicker from './timezone';
import { connect } from 'react-redux';
import { IState } from '../../store';
import { updateExpandedMyself } from '../../features/myself/actions';

type AccountProps = {
  updateExpandedMyself: () => void;
};

class Account extends React.Component<AccountProps> {
  componentDidMount() {
    this.props.updateExpandedMyself();
  }
  render() {
    return (
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <span>Time Zone &nbsp;&nbsp;&nbsp;</span> <TimezonePicker />
      </div>
    );
  }
}

const mapStateToProps = (state: IState) => ({
  timezone: state.myself.timezone
});

export default connect(mapStateToProps, { updateExpandedMyself })(Account);
