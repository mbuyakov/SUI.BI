import {useEmotionCss} from '@ant-design/use-emotion-css';
import {useModel} from '@umijs/max';
import React from 'react';

export type SiderTheme = 'light' | 'dark';

const GlobalHeaderRight: React.FC = () => {
  const className = useEmotionCss(() => {
    return {
      display: 'flex',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      gap: 8,
    };
  });

  const {initialState} = useModel('@@initialState');

  if (!initialState || !initialState.settings) {
    return null;
  }

  return (<div className={className}/>);
};

export default GlobalHeaderRight;
