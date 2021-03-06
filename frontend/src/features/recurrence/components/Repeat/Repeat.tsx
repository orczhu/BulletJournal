import React from 'react';
import { Select } from 'antd';
import RepeatYearly from './RepeatYearly';
import RepeatMonthly from './RepeatMonthly';
import RepeatWeekly from './RepeatWeekly';
import RepeatDaily from './RepeatDaily';
import RepeatHourly from './RepeatHourly';
//used for redux
import { IState } from '../../../../store';
import { connect } from 'react-redux';
//used for interface
import {
  Daily,
  Hourly,
  Weekly,
  MonthlyOn,
  MonthlyOnThe,
  YearlyOn,
  YearlyOnThe
} from '../../interface';
//used for action
import {
  updateStartString,
  updateRepeatYearlyOn,
  updateRepeatYearlyOnThe,
  updateRepeatDaily,
  updateRepeatHourly,
  updateRepeatMonthlyOn,
  updateRepeatMonthlyOnThe,
  updateRepeatWeekly
} from '../../actions';
const { Option } = Select;

type RepeatProps = {
  startDate: string;
  startTime: string;
  repeatHourly: any;
  repeatDaily: any;
  repeatWeekly: any;
  repeatMonthlyOnThe: any;
  repeatMonthlyOn: any;
  repeatYearlyOnThe: any;
  repeatYearlyOn: any;
  monthlyOn: boolean;
  yearlyOn: boolean;
  updateStartString: (startDate: string, startTime: string) => void;
  updateRepeatYearlyOn: (repeatYearlyOn: YearlyOn) => void;
  updateRepeatYearlyOnThe: (repeatYearlyOnThe: YearlyOnThe) => void;
  updateRepeatMonthlyOn: (repeatMonthlyOn: MonthlyOn) => void;
  updateRepeatMonthlyOnThe: (repeatMonthlyOnThe: MonthlyOnThe) => void;
  updateRepeatWeekly: (repeatWeekly: Weekly) => void;
  updateRepeatDaily: (repeatDaily: Daily) => void;
  updateRepeatHourly: (repeatHourly: Hourly) => void;
};

type SelectState = {
  value: string;
};

class Repeat extends React.Component<RepeatProps, SelectState> {
  state: SelectState = {
    value: 'Yearly'
  };

  componentDidMount = () => {
    this.props.updateStartString(this.props.startDate, this.props.startTime);
    this.props.updateRepeatYearlyOn(this.props.repeatYearlyOn);
  };

  onChangeValue = (value: string) => {
    this.setState({ value: value });
    if (value === 'Yearly') {
      if (this.props.yearlyOn) {
        this.props.updateRepeatYearlyOn(this.props.repeatYearlyOn);
      } else {
        this.props.updateRepeatYearlyOnThe(this.props.repeatYearlyOnThe);
      }
    } else if (value === 'Monthly') {
      if (this.props.monthlyOn) {
        this.props.updateRepeatMonthlyOn(this.props.repeatMonthlyOn);
      } else {
        this.props.updateRepeatMonthlyOnThe(this.props.repeatMonthlyOnThe);
      }
    } else if (value === 'Weekly') {
      this.props.updateRepeatWeekly(this.props.repeatWeekly);
    } else if (value === 'Daily') {
      this.props.updateRepeatDaily(this.props.repeatDaily);
    } else if (value === 'Hourly') {
      this.props.updateRepeatHourly(this.props.repeatHourly);
    }
  };

  render() {
    return (
      <div>
        <div
          style={{ display: 'flex', alignItems: 'center', paddingBottom: 24 }}
        >
          <label style={{ marginRight: '1em' }}>
            <strong>Repeat : </strong>
          </label>
          <Select
            placeholder='Choose a type'
            style={{ width: '30%' }}
            value={this.state.value}
            onChange={e => this.onChangeValue(e)}
          >
            <Option value='Yearly'>Yearly</Option>
            <Option value='Monthly'>Monthly</Option>
            <Option value='Weekly'>Weekly</Option>
            <Option value='Daily'>Daily</Option>
            <Option value='Hourly'>Hourly</Option>
          </Select>
        </div>
        {this.state.value === 'Yearly' && <RepeatYearly />}
        {this.state.value === 'Monthly' && <RepeatMonthly />}
        {this.state.value === 'Weekly' && <RepeatWeekly />}
        {this.state.value === 'Daily' && <RepeatDaily />}
        {this.state.value === 'Hourly' && <RepeatHourly />}
      </div>
    );
  }
}

const mapStateToProps = (state: IState) => ({
  startTime: state.rRule.startTime,
  startDate: state.rRule.startDate,
  monthlyOn: state.rRule.monthlyOn,
  yearlyOn: state.rRule.yearlyOn,
  repeatYearlyOn: state.rRule.repeatYearlyOn,
  repeatYearlyOnThe: state.rRule.repeatYearlyOnThe,
  repeatMonthlyOn: state.rRule.repeatMonthlyOn,
  repeatMonthlyOnThe: state.rRule.repeatMonthlyOnThe,
  repeatWeekly: state.rRule.repeatWeekly,
  repeatDaily: state.rRule.repeatDaily,
  repeatHourly: state.rRule.repeatHourly
});
export default connect(mapStateToProps, {
  updateStartString,
  updateRepeatYearlyOn,
  updateRepeatYearlyOnThe,
  updateRepeatDaily,
  updateRepeatHourly,
  updateRepeatMonthlyOn,
  updateRepeatMonthlyOnThe,
  updateRepeatWeekly
})(Repeat);
