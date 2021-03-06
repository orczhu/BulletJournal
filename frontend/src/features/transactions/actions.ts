import { actions } from './reducer';
import { History } from 'history';
import { FrequencyType, LedgerSummaryType } from './interface';

export const updateTransactions = (
  projectId: number,
  timezone: string,
  ledgerSummaryType: string,
  frequencyType?: string,
  startDate?: string,
  endDate?: string
) =>
  actions.TransactionsUpdate({
    projectId: projectId,
    timezone: timezone,
    frequencyType: frequencyType,
    ledgerSummaryType: ledgerSummaryType,
    startDate: startDate,
    endDate: endDate,
  });
export const createTransaction = (
  projectId: number,
  amount: number,
  name: string,
  payer: string,
  date: string,
  transactionType: number,
  timezone: string,
  time?: string
) =>
  actions.TransactionsCreate({
    projectId: projectId,
    amount: amount,
    name: name,
    payer: payer,
    date: date,
    transactionType: transactionType,
    timezone: timezone,
    time: time,
  });
export const getTransaction = (transactionId: number) =>
  actions.TransactionGet({ transactionId: transactionId });
export const deleteTransaction = (transactionId: number) =>
  actions.TransactionDelete({ transactionId: transactionId });
export const patchTransaction = (
  transactionId: number,
  amount: number,
  name: string,
  payer: string,
  date: string,
  time: string,
  transactionType: number,
  timezone: string
) =>
  actions.TransactionPatch({
    transactionId: transactionId,
    amount: amount,
    name: name,
    payer: payer,
    date: date,
    time: time,
    transactionType: transactionType,
    timezone: timezone,
  });
export const setTransactionLabels = (transactionId: number, labels: number[]) =>
  actions.TransactionSetLabels({
    transactionId: transactionId,
    labels: labels,
  });

export const updateTransactionVisible = (visible: boolean) =>
  actions.UpdateAddTransactionVisible({ visible: visible });

export const shareTransaction = (
  transactionId: number,
  targetUser: string,
  targetGroup: number,
  generateLink: boolean
) =>
  actions.TransactionShare({
    transactionId: transactionId,
    targetUser: targetUser,
    targetGroup: targetGroup,
    generateLink: generateLink,
  });

export const moveTransaction = (
  transactionId: number,
  targetProject: number,
  history: History
) =>
  actions.TransactionMove({
    transactionId: transactionId,
    targetProject: targetProject,
    history: history,
  });

export const updateTransactionContents = (transactionId: number) =>
  actions.TransactionContentsUpdate({ transactionId: transactionId });
export const updateTransactionContentRevision = (
  transactionId: number,
  contentId: number,
  revisionId: number
) =>
  actions.TransactionContentRevisionUpdate({
    transactionId: transactionId,
    contentId: contentId,
    revisionId: revisionId,
  });

export const createContent = (transactionId: number, text: string) =>
  actions.TransactionContentCreate({
    transactionId: transactionId,
    text: text,
  });

export const deleteContent = (transactionId: number, contentId: number) =>
  actions.TransactionContentDelete({
    transactionId: transactionId,
    contentId: contentId,
  });
export const patchContent = (
  transactionId: number,
  contentId: number,
  text: string
) =>
  actions.TransactionContentPatch({
    transactionId: transactionId,
    contentId: contentId,
    text: text,
  });

export const updateTransactionForm = (
  startDate?: string,
  endDate?: string,
  frequencyType?: FrequencyType,
  ledgerSummaryType?: LedgerSummaryType,
  timezone?: string
) =>
  actions.TransactionFormUpdate({
    startDate,
    endDate,
    frequencyType,
    ledgerSummaryType,
    timezone,
  });
