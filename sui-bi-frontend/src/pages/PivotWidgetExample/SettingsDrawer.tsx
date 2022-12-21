import React, {useCallback, useEffect, useMemo, useState} from "react";
import {Button, Drawer, Form, Input, Modal, Select, SelectProps} from "antd";
import {useForm} from "antd/lib/form/Form";
import {getTablesByDatabaseId} from "@/services/api/TableApi";
import {DefaultOptionType} from "rc-select/lib/Select";
import {getColumnsByTableId} from "@/services/api/ColumnApi";
import {IPivotWidgetExampleSettings, IPivotWidgetExampleValue} from "@/pages/PivotWidgetExample/types";
import {groupBy} from "lodash";
import {OneTagPerRowSelect, IOneTagPerRowSelectProps} from "@/components/DataEntry/OneTagPerRowSelect";
import {DatabaseOutlined, EditOutlined, PlusOutlined} from "@ant-design/icons";
import {CustomTagProps} from "rc-select/lib/BaseSelect";
import {draw} from "@/utils";

import styles from "./SettingsDrawer.less";

export interface ISettingsDrawerProps {
  open: boolean;
  onClose(): void;
  onSubmit(values: IPivotWidgetExampleSettings): void;
}

export function SettingsDrawer(props: ISettingsDrawerProps): JSX.Element {
  const {open, onClose, onSubmit} = props;

  const [form] = useForm();

  return (
    <Drawer
      title="Настройки"
      open={open}
      onClose={onClose}
      maskClosable={false}
      extra={(
        <Button
          type="primary"
          onClick={() => {
            form.validateFields()
              .then(onSubmit)
              .catch((reason) => console.warn("Validate Failed:", reason));
          }}
        >
          Применить
        </Button>
      )}
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="table"
          label="Таблица"
          rules={[{required: true, message: "Поле должно быть заполнено"}]}
        >
          <TableSelect
            onChange={() => form.setFieldsValue({columns: [], rows: [], values: []})}
          />
        </Form.Item>
        <Form.Item
          dependencies={["table", "columns"]}
          noStyle={true}
        >
          {() => {
            const table = form.getFieldValue("table");
            const columns: string[] = form.getFieldValue("columns") || [];

            return (
              <Form.Item
                name="rows"
                label="Строки"
                rules={[{required: true, type: "array", message: "Поле должно быть заполнено"}]}
              >
                <DimensionsSelect
                  table={table}
                  disableColumns={columns}
                />
              </Form.Item>
            );
          }}
        </Form.Item>
        <Form.Item
          dependencies={["table", "rows"]}
          noStyle={true}
        >
          {() => {
            const table = form.getFieldValue("table");
            const rows: string[] = form.getFieldValue("rows") || [];

            return (
              <Form.Item
                name="columns"
                label="Колонки"
                rules={[{required: true, type: "array", message: "Поле должно быть заполнено"}]}
              >
                <DimensionsSelect
                  table={table}
                  disableColumns={rows}
                />
              </Form.Item>
            );
          }}
        </Form.Item>
        <Form.Item
          dependencies={["table"]}
          noStyle={true}
        >
          {() => {
            const table = form.getFieldValue("table");

            return (
              <Form.Item
                name="values"
                label="Значения"
                rules={[{required: true, type: "array", message: "Поле должно быть заполнено"}]}
              >
                <ValuesSelect table={table}/>
              </Form.Item>
            );
          }}
        </Form.Item>
      </Form>
    </Drawer>
  );
}

type ITableSelectProps = Omit<SelectProps<string>, "children" | "options" | "mode" | "showSearch" | "optionFilterProp" | "filterOption">;

function TableSelect(props: ITableSelectProps): JSX.Element {
  const [tables, setTables] = useState<API.TableDto[]>();

  const loading = (tables == null);

  const options: DefaultOptionType[] = useMemo(() => {
    return Object.values(groupBy(tables || [], it => it.tableSchema))
      .sort((a, b) => {
        const aTableSchema = a[0].tableSchema;
        const bTableSchema = b[0].tableSchema;

        if (!aTableSchema) {
          return 1;
        }

        if (!bTableSchema) {
          return 0;
        }

        return aTableSchema.localeCompare(bTableSchema);
      })
      .map(schemaTables => ({
        label: schemaTables[0].tableSchema || "Без схемы",
        options: schemaTables
          .sort((a, b) => a.tableName.localeCompare(b.tableName))
          .map(it => ({value: String(it.id), label: it.tableName}))
      }));
  }, [tables]);

  useEffect(() => {
    const databaseId = "1";

    getTablesByDatabaseId({databaseId: Number(databaseId)})
      .then(setTables)
      .catch((reason) => console.error(`Error in getTablesByDatabaseId(databaseId=${databaseId})`, reason));
  }, [setTables]);

  return (
    <Select
      {...props}
      loading={props.loading || loading}
      children={undefined}
      options={options}
      mode={undefined}
      showSearch={true}
      optionFilterProp="label"
      filterOption={undefined}
    />
  );
}

interface IDimensionsSelectProps extends Omit<IOneTagPerRowSelectProps<string[]>, "children" | "options" | "mode" | "showSearch" | "optionFilterProp" | "filterOption"> {
  table?: string;
  disableColumns?: string[];
}

function DimensionsSelect(props: IDimensionsSelectProps): JSX.Element {
  const {table, disableColumns, ...selectProps} = props;

  const [columns, setColumns] = useState<API.ColumnDto[]>();

  const loading = (columns == null);

  const options: DefaultOptionType[] = useMemo(() => {
    return (columns || [])
      .map(it => ({
        value: String(it.id),
        label: it.columnName,
        disabled: disableColumns && disableColumns.includes(String(it.id))
      }))
      .sort((a, b) => {
        if (a.disabled !== b.disabled) {
          return Number(b.disabled) - Number(a.disabled);
        }

        return a.label.localeCompare(b.label);
      });
  }, [columns, disableColumns]);

  const tagCustomizer = useCallback(() => ({icon: <DatabaseOutlined/>, color: "blue"}), []);

  useEffect(() => {
    if (!table) {
      setColumns([]);
      return;
    }

    setColumns(undefined);

    getColumnsByTableId({tableId: Number(table)})
      .then(setColumns)
      .catch((reason) => console.error(`Error in getColumnsByTableId(tableId=${table})`, reason));
  }, [table, setColumns]);

  return (
    <OneTagPerRowSelect
      {...selectProps}
      disabled={selectProps.disabled || !table}
      loading={selectProps.loading || loading}
      children={undefined}
      options={options}
      showSearch={true}
      optionFilterProp="label"
      filterOption={undefined}
      tagCustomizer={tagCustomizer}
    />
  );
}

interface IValuesSelectProps {
  table?: string;
  value?: IPivotWidgetExampleValue[] | null;
  onChange?(value: IPivotWidgetExampleValue[]): void;
}

function ValuesSelect(props: IValuesSelectProps): JSX.Element {
  const {table, value: propsValue, onChange: propsOnChange} = props;

  const [columns, setColumns] = useState<API.ColumnDto[]>();

  useEffect(() => {
    if (!table) {
      setColumns([]);
      return;
    }

    setColumns(undefined);

    getColumnsByTableId({tableId: Number(table)})
      .then(setColumns)
      .catch((reason) => console.error(`Error in getColumnsByTableId(tableId=${table})`, reason));
  }, [table, setColumns]);

  const selectValue = useMemo(() => {
    return [
      ...(propsValue || []).map((_, index) => String(index)),
      "__ADD__"
    ];
  }, [propsValue]);

  const selectOnChange = useCallback((v: string[]) => {
    if (propsOnChange) {
      const newValue = (propsValue || []).filter((_, index) => v.includes(String(index)));
      propsOnChange(newValue);
    }
  }, [propsOnChange]);

  const disabled = !table;

  const loading = !columns;

  const options = useMemo(() => {
    return [
      ...(propsValue || []).map((it, index): DefaultOptionType => ({
        value: String(index),
        label: it.label
      })),
      {
        value: "__ADD__",
        label: "Нажмите, чтобы добавить",
        disabled: true
      }
    ]
  }, [columns, propsValue]);

  const tagCustomizer = useCallback((customTagProps: CustomTagProps) => {
    if (customTagProps.value === "__ADD__") {
      const drawAddModal = () => {
        draw(
          <AddOrEditValueModal
            title="Добавление значения"
            columns={columns || []}
            onSubmit={(values) => {
              if (propsOnChange) {
                propsOnChange([...(propsValue || []), values]);
              }
            }}
          />
        );
      };

      return {
        icon: <PlusOutlined/>,
        style:  disabled ? undefined : {cursor: "pointer"},
        onClick: disabled ? undefined : drawAddModal
      };
    }

    const drawEditModal = () => {
      draw(
        <AddOrEditValueModal
          title="Изменение значения"
          columns={columns || []}
          initialValues={propsValue?.find((it, index) => String(index) == customTagProps.value)}
          onSubmit={(values) => {
            if (propsOnChange) {
              const newValue = (propsValue || []).map((it, index) => (String(index) === customTagProps.value) ? values : it);
              propsOnChange(newValue);
            }
          }}
        />
      );
    };

    return {
      icon: <EditOutlined/>,
      color: "blue",
      style: disabled ? undefined : {cursor: "pointer"},
      onClick: disabled ? undefined : drawEditModal
    };
  }, [disabled, propsValue, propsOnChange, columns]);

  return (
    <OneTagPerRowSelect
      className={styles.hideSelectSuffix}
      value={selectValue}
      onChange={selectOnChange}
      options={options}
      disabled={disabled}
      loading={loading}
      dropdownStyle={{display: "none"}}
      suffixIcon={null}
      tagCustomizer={tagCustomizer}
    />
  );
}

interface IAddOrEditValueModalProps {
  columns: API.ColumnDto[];
  title: string;
  initialValues?: Partial<IPivotWidgetExampleValue>;
  onSubmit(values: IPivotWidgetExampleValue): void;
}

function AddOrEditValueModal(props: IAddOrEditValueModalProps): JSX.Element {
  const {columns, title, initialValues: propsInitialValues, onSubmit} = props;

  const [form] = Form.useForm();
  const [open, setOpen] = useState(true);

  const closeModal = useCallback(() => setOpen(false), [setOpen])

  const typeOptions = useMemo(() => [
    {
      value: "COUNT",
      label: "Количество (COUNT)",
      field: "none",
      createValue(label: string): IPivotWidgetExampleValue {
        return {type: "COUNT", label};
      }
    },
    // {
    //   value: "COUNT_DISTINCT",
    //   label: "Количество уникальных (COUNT DISTINCT)",
    //   field: "optional",
    //   createValue(label: string, field: string | null): IPivotWidgetExampleValue {
    //     return field ? {type: "COUNT_DISTINCT_BY_FIELD", field, label} : {type: "COUNT_DISTINCT", label};
    //   }
    // },
    {
      value: "SUM",
      label: "Сумма (SUM)",
      field: "required",
      createValue(label: string, field: string): IPivotWidgetExampleValue {
        return {type: "SUM_BY_FIELD", field, label};
      }
    },
    {
      value: "MIN",
      label: "Минимум (MIN)",
      field: "required",
      createValue(label: string, field: string): IPivotWidgetExampleValue {
        return {type: "MIN_BY_FIELD", field, label};
      }
    },
    {
      value: "MAX",
      label: "Максимум (MAX)",
      field: "required",
      createValue(label: string, field: string): IPivotWidgetExampleValue {
        return {type: "MAX_BY_FIELD", field, label};
      }
    },
    {
      value: "AVERAGE",
      label: "Среднее (AVERAGE)",
      field: "required",
      createValue(label: string, field: string): IPivotWidgetExampleValue {
        return {type: "AVERAGE_BY_FIELD", field, label};
      }
    }
  ], []);

  const fieldOptions: DefaultOptionType[] = useMemo(() => columns.map(it => ({value: String(it.id), label: it.columnName})), [columns]);

  const initialValues = useMemo(() => {
    switch (propsInitialValues?.type) {
      case "COUNT":
        return {type: "COUNT", label: propsInitialValues.label};
      case "COUNT_DISTINCT":
        return {type: "COUNT_DISTINCT", label: propsInitialValues.label};
      case "COUNT_DISTINCT_BY_FIELD":
        return {type: "COUNT_DISTINCT", field: propsInitialValues.field, label: propsInitialValues.label};
      case "SUM_BY_FIELD":
        return {type: "SUM", field: propsInitialValues.field, label: propsInitialValues.label};
      case "MIN_BY_FIELD":
        return {type: "MIN", field: propsInitialValues.field, label: propsInitialValues.label};
      case "MAX_BY_FIELD":
        return {type: "MAX", field: propsInitialValues.field, label: propsInitialValues.label};
      case "AVERAGE_BY_FIELD":
        return {type: "AVERAGE", field: propsInitialValues.field, label: propsInitialValues.label};
      default:
        return undefined;
    }
  }, [propsInitialValues]);

  const onOk = useCallback(() => {
    form.validateFields()
      .then((values) => {
        const typeOption = typeOptions.find(it => it.value == values.type);

        if (typeOption) {
          onSubmit(typeOption.createValue(values.label, values.field ?? null));
        }

        closeModal();
      })
      .catch((reason) => console.warn("Validate Failed:", reason));
  }, [onSubmit, form, closeModal, typeOptions]);

  return (
    <Modal
      title={title}
      open={open}
      destroyOnClose={true}
      okText="Сохранить"
      cancelText="Отмена"
      onOk={onOk}
      onCancel={closeModal}
      transitionName=""
      maskTransitionName=""
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={initialValues}
      >
        <Form.Item
          name="label"
          label="Наименование"
          rules={[{required: true, message: "Поле должно быть заполнено"}]}
        >
          <Input/>
        </Form.Item>
        <Form.Item
          name="type"
          label="Тип"
          rules={[{required: true, message: "Поле должно быть заполнено"}]}
        >
          <Select
            options={typeOptions}
            onChange={(typeValue) => {
              const typeOption = typeValue ? typeOptions.find(it => it.value === typeValue) : undefined;

              if (typeOption != null && typeOption.field === "none") {
                form.setFieldValue("field", undefined);
              }
            }}
          />
        </Form.Item>
        <Form.Item noStyle={true} dependencies={["type"]}>
          {() => {
            const typeValue = form.getFieldValue("type");
            const typeOption = typeOptions.find(it => it.value === typeValue);

            const disabled = typeOption == null || typeOption.field === "none";
            const required = typeOption != null && typeOption.field === "required";

            return (
              <Form.Item
                name="field"
                label="Поле"
                rules={required ? [{required: true, message: "Поле должно быть заполнено"}] : undefined}
              >
                <Select
                  options={fieldOptions}
                  showSearch={true}
                  optionFilterProp="label"
                  disabled={disabled}
                />
              </Form.Item>
            );
          }}
        </Form.Item>
      </Form>
    </Modal>
  );
}
