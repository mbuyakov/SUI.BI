import {BaseOptionType, DefaultOptionType} from "rc-select/lib/Select";
import {Select, SelectProps, Tag} from "antd";
import {CustomTagProps} from "rc-select/lib/BaseSelect";
import * as React from "react";
import {LiteralUnion} from "antd/es/_util/type";
import {PresetColorType, PresetStatusColorType} from "antd/es/_util/colors";
import classNames from "classnames";

import styles from "./OneTagPerRowSelect.less";
import {useCallback} from "react";

export interface ITagCustomizerResult {
  className?: string;
  color?: LiteralUnion<PresetColorType | PresetStatusColorType, string>;
  style?: React.CSSProperties;
  icon?: React.ReactNode;
  onClick?(value: any): void;
}

export interface IOneTagPerRowSelectProps<ValueType = any, OptionType extends BaseOptionType = DefaultOptionType>
  extends Omit<SelectProps<ValueType, OptionType>, "mode" | "tag"> {
  tagCustomizer?(props: CustomTagProps): ITagCustomizerResult;
}

export function OneTagPerRowSelect<ValueType = any, OptionType extends BaseOptionType = DefaultOptionType>(props: IOneTagPerRowSelectProps<ValueType, OptionType>): JSX.Element {
  const {tagCustomizer, ...selectProps} = props;

  const tagRender = useCallback((props: CustomTagProps) => fullWidthTagRender(props, tagCustomizer), [tagCustomizer]);

  return (
    <Select
      {...selectProps}
      className={classNames(selectProps.className, styles.selectStyle)}
      mode="multiple"
      tagRender={tagRender}
      suffixIcon={null}
    />
  );
}

function fullWidthTagRender(props: CustomTagProps, tagCustomizer?: (props: CustomTagProps) => ITagCustomizerResult): JSX.Element {
  const {label, value, onClose, closable} = props;
  const tagCustomizerResult = tagCustomizer?.(props);

  const onClick = useCallback(() => {
    if (tagCustomizerResult && tagCustomizerResult.onClick) {
      tagCustomizerResult.onClick(value);
    }
  }, [value, tagCustomizerResult])

  return (
    <Tag
      icon={tagCustomizerResult?.icon}
      className={classNames(tagCustomizerResult?.className, styles.tagStyle)}
      closable={closable}
      onClose={onClose}
      children={(<span>{label}</span>)}
      style={{...tagCustomizerResult?.style, width: "100%"}}
      color={tagCustomizerResult?.color}
      onClick={onClick}
    />
  );
}
